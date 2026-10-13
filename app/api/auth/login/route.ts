import { NextRequest } from "next/server";
import { authenticate } from "@/lib/auth-server";

export function POST(request: NextRequest) {
  return authenticate(request, "login");
}
