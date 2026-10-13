import { NextResponse } from "next/server";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth-server";

export function POST() {
  const response = NextResponse.json({ success: true });
  response.cookies.set(ACCESS_TOKEN_COOKIE, "", {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: 0,
  });
  return response;
}
