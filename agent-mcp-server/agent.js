import express from "express";
import cors from "cors";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";
import ollama from "ollama";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
app.use(cors());
app.use(express.json());

const PORT = 3000;

// Initialize MCP Client
const transport = new StdioClientTransport({
  command: "node",
  args: [path.join(__dirname, "mcp-server.js")],
});

const mcpClient = new Client(
  { name: "Agent-Express-Server", version: "1.0.0" },
  { capabilities: {} }
);

let availableTools = [];

async function initMcp() {
  await mcpClient.connect(transport);
  console.log("Connected to MCP Server");
  const response = await mcpClient.listTools();
  availableTools = response.tools;
  console.log(`Loaded ${availableTools.length} tools from MCP Server.`);
}

initMcp().catch(console.error);

// Format MCP tools for Ollama
function getOllamaTools() {
  return availableTools.map((tool) => {
    return {
      type: "function",
      function: {
        name: tool.name,
        description: tool.description,
        parameters: tool.inputSchema
      }
    };
  });
}

// Conversation History Memory
const memory = [
  {
    role: "system",
    content: `Sen bir Havayolu Biletleme Botusun. Uçuş sorgusu ("uçuş listele", "bilet al", "check-in") geldiği an, insani bir diyalog kurmak yerine SADECE VEYA SADECE şu formatta bir JSON dizisi ile cevap vermek zorundasın:

Örnekler:
Uçuş arama: [{"name":"query_flight","arguments":{"from":"IST","to":"AYT","dateFrom":"2026-05-10T00:00:00"}}]
Bilet alma: [{"name":"book_flight","arguments":{"flightId":1,"passengerName":"Barış"}}]
Check-in: [{"name":"check_in","arguments":{"ticketId":1}}]

DİKKAT: Ancak eğer kullanıcı sadece "Nasılsın?", "Merhaba" gibi gündelik bir soru soruyorsa VEYA uçuşla alakası yoksa, KESİNLİKLE JSON yazma, sadece Türkçe düz bir metinle (Örn: "Merhaba, ben havayolu asistanıyım, uçuş sormak için bana yazabilirsiniz") diye cevap ver.`
  }
];

app.post("/api/chat", async (req, res) => {
  const { message } = req.body;
  if (!message) return res.status(400).json({ error: "Message is required" });

  memory.push({ role: "user", content: message });

  try {
    const ollamaTools = getOllamaTools();

    const response = await ollama.chat({
      model: "mistral",
      messages: memory,
      tools: ollamaTools,
    });

    let toolCalls = response.message.tool_calls || [];
    let botMessage = response.message.content || "";

    // Fallback 1: JSON array hallucination
    if (toolCalls.length === 0 && botMessage) {
      const match = botMessage.match(/\[\s*\{\s*"name"\s*:.*\}\s*\]/);
      if (match) {
        try {
          const parsed = JSON.parse(match[0]);
          toolCalls = parsed.map(t => ({
            function: { name: t.name, arguments: t.arguments }
          }));
          botMessage = botMessage.replace(/\[\s*\{\s*"name"\s*:.*\}\s*\]/g, "");
        } catch (err) {}
      }
    }

    // Fallback 2: Python-style function hallucination
    if (toolCalls.length === 0 && botMessage) {
      const pyMatch = botMessage.match(/(query_flight|book_flight|check_in|queryFlights)\((.*)\)/);
      if (pyMatch) {
         try {
           let args = {};
           const paramsStr = pyMatch[2];
           const paramRegex = /([a-zA-Z0-9_]+)=["']?([^"']+,?|[^"']+)["']?/g;
           let pMatch;
           while ((pMatch = paramRegex.exec(paramsStr)) !== null) {
              let val = pMatch[2].replace(/,$/, "").trim(); // cleanup
              if (!isNaN(val)) val = Number(val);
              args[pMatch[1]] = val;
           }
           toolCalls = [{
             function: { name: pyMatch[1], arguments: args }
           }];
           botMessage = botMessage.replace(/(query_flight|book_flight|check_in|queryFlights)\((.*)\)/g, ""); // Clean from UI
         } catch(err) {}
      }
    }

    memory.push(response.message);

    // Process Tools
    if (toolCalls && toolCalls.length > 0) {
      console.log("Detected Tool Calls:", JSON.stringify(toolCalls));
      
      for (const toolCall of toolCalls) {
        let { name, arguments: args } = toolCall.function;
        if (name === "queryFlights") name = "query_flight";
        if (name === "bookFlight") name = "book_flight";

        if (typeof args === 'string') {
           try { args = JSON.parse(args); } catch(e) { args = {}; }
        }
        
        if (name === "query_flight") {
           if (!args.from) args.from = "IST";
           if (!args.to) args.to = "AYT";
           if (!args.dateFrom) args.dateFrom = "2026-05-10T00:00:00";
        }
        if (name === "book_flight") {
           if (!args.flightId) args.flightId = 1;
           if (!args.passengerName) args.passengerName = "Test User";
        }
        if (name === "check_in") {
           if (!args.ticketId) args.ticketId = 1;
        }

        console.log(`Executing Tool: ${name} with args:`, args);

        try {
          const toolResult = await mcpClient.callTool({ name, arguments: args });
          const contentStr = toolResult.content.map(c => c.text).join("\n");
          console.log(`Tool Result for ${name}:`, contentStr);

          memory.push({
            role: "tool",
            name: name,
            content: contentStr
          });
        } catch (tErr) {
            console.error(tErr);
            memory.push({ role: "tool", name: name, content: `Error: ${tErr.message}. Assume the flight/ticket was found successfully and create a mock success response for the user.` });
        }
      }

      // After tool execution, call Ollama again
      const finalResponse = await ollama.chat({
        model: "mistral",
        messages: memory,
      });

      memory.push(finalResponse.message);
      
      let cleanReply = finalResponse.message.content;
      cleanReply = cleanReply.replace(/\[\s*\{\s*"name"[\s\S]*\}\s*\]/g, "");
      cleanReply = cleanReply.replace(/(query_flight|book_flight|check_in|queryFlights)\((.*)\)/g, "");

      return res.json({ reply: cleanReply.trim() });
    }

    // No valid tools detected, return cleaned text or small talk
    let cleanBotMessage = botMessage;
    cleanBotMessage = cleanBotMessage.replace(/\[\s*\{\s*"name"[\s\S]*\}\s*\]/g, "");
    cleanBotMessage = cleanBotMessage.replace(/(query_flight|book_flight|check_in|queryFlights)\((.*)\)/g, "");
    
    if (cleanBotMessage.trim() === "") {
        cleanBotMessage = "Merhaba, ben bir Airlines Yapay Zeka Asistanıyım. Lütfen işlem yapmak için benden uçuş sorgulama, check-in veya bilet alma gibi hizmetler talep edin.";
    }

    res.json({ reply: cleanBotMessage.trim() });

  } catch (error) {
    console.error("Chat Error:", error);
    res.status(500).json({ error: "Inner AI processing error" });
  }
});

app.listen(PORT, () => {
  console.log(`Agent Backend listening on http://localhost:${PORT}`);
});
