import { NextRequest, NextResponse } from "next/server";
import { ACCESS_TOKEN_COOKIE } from "@/lib/auth-server";

const BACKEND_URL = process.env.GIRO_BACKEND_URL ?? "http://localhost:8080";

async function forward(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  if (!["GET", "HEAD", "OPTIONS"].includes(request.method)
      && request.headers.get("origin") !== request.nextUrl.origin) {
    return NextResponse.json({ message: "Origem da requisição não permitida." }, { status: 403 });
  }
  const token = request.cookies.get(ACCESS_TOKEN_COOKIE)?.value;
  if (!token) {
    return NextResponse.json({ message: "Autenticação necessária." }, { status: 401 });
  }

  const { path } = await context.params;
  const backendUrl = new URL(`/${path.map(encodeURIComponent).join("/")}`, BACKEND_URL);
  backendUrl.search = new URL(request.url).search;
  const headers = new Headers(request.headers);
  headers.delete("cookie");
  headers.delete("host");
  headers.set("Authorization", `Bearer ${token}`);

  try {
    const backendResponse = await fetch(backendUrl, {
      method: request.method,
      headers,
      body: ["GET", "HEAD"].includes(request.method) ? undefined : await request.arrayBuffer(),
      cache: "no-store",
      redirect: "manual",
    });
    const responseHeaders = new Headers();
    const contentType = backendResponse.headers.get("content-type");
    if (contentType) responseHeaders.set("content-type", contentType);
    return new Response(backendResponse.body, {
      status: backendResponse.status,
      headers: responseHeaders,
    });
  } catch {
    return NextResponse.json({ message: "Serviço de API indisponível." }, { status: 502 });
  }
}

export const GET = forward;
export const POST = forward;
export const PUT = forward;
export const PATCH = forward;
export const DELETE = forward;
