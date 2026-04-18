import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import axios from "axios";

// API Gateway base URL
const GATEWAY_URL = "http://localhost:8081/api/v1/flights";

const server = new McpServer({
  name: "Airline-API-MCP-Server",
  version: "1.0.0"
});

// Tool 1: Query Flight
server.tool(
  "query_flight",
  "Query available flights between two locations on a specific date.",
  {
    from: z.string().describe("Departure location (e.g. IST)"),
    to: z.string().describe("Arrival location (e.g. AYT)"),
    dateFrom: z.string().describe("Date in ISO format (e.g. 2026-05-10T00:00:00)"),
    page: z.number().optional().describe("Page number for pagination")
  },
  async ({ from, to, dateFrom, page }) => {
    try {
      const response = await axios.get(`${GATEWAY_URL}/query`, {
        params: { from, to, dateFrom, page: page || 0 }
      });
      return {
        content: [{ type: "text", text: JSON.stringify(response.data) }]
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: `Error querying flight: ${error.message}` }],
        isError: true
      };
    }
  }
);

// Tool 2: Book Flight
server.tool(
  "book_flight",
  "Buy a ticket for a specific flight using its flightId.",
  {
    flightId: z.number().describe("The ID of the flight to book"),
    passengerName: z.string().describe("Name of the passenger")
  },
  async ({ flightId, passengerName }) => {
    try {
      const response = await axios.post(`${GATEWAY_URL}/buy-ticket`, null, {
        params: { flightId, passengerName },
        headers: { Authorization: "Bearer Agent-Token" }
      });
      return {
        content: [{ type: "text", text: `Ticket booked successfully: ${JSON.stringify(response.data)}` }]
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: `Error booking flight: ${error.message}` }],
        isError: true
      };
    }
  }
);

// Tool 3: Check In
server.tool(
  "check_in",
  "Perform check-in for a booked ticket and assign a seat.",
  {
    ticketId: z.number().describe("The ID of the ticket to check in")
  },
  async ({ ticketId }) => {
    try {
      const response = await axios.post(`${GATEWAY_URL}/check-in/${ticketId}`, null, {
        headers: { Authorization: "Bearer Agent-Token" }
      });
      // Usually returns a string indicating success or the seat number
      return {
        content: [{ type: "text", text: `Check-in successful: ${response.data}` }]
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: `Error during check-in: ${error.message}` }],
        isError: true
      };
    }
  }
);

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("MCP Server running on stdio");
}

main().catch(console.error);
