import { NextRequest, NextResponse } from "next/server";

export const ACCESS_TOKEN_COOKIE = "giro_access_token";
const BACKEND_URL = process.env.GIRO_BACKEND_URL ?? "http://localhost:8080";

export async function authenticate(request: NextRequest, action: "login" | "register") {
  let payload: unknown;
  try {
    payload = await request.json();
  } catch {
    return NextResponse.json({ message: "Corpo da requisição inválido." }, { status: 400 });
  }

  let backendResponse: Response;
  try {
    backendResponse = await fetch(`${BACKEND_URL}/api/auth/${action}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      cache: "no-store",
    });
  } catch {
    return NextResponse.json({ message: "Serviço de autenticação indisponível." }, { status: 502 });
  }

  const result = await backendResponse.json().catch(() => null) as {
    token?: string;
    expiresIn?: number;
    user?: { id: string; name: string; email: string };
    message?: string;
    detail?: string;
  } | null;

  if (!backendResponse.ok) {
    return NextResponse.json(
      { message: result?.message ?? result?.detail ?? "Não foi possível autenticar." },
      { status: backendResponse.status },
    );
  }
  if (!result?.token || !result.expiresIn || !result.user) {
    return NextResponse.json({ message: "Resposta inválida do serviço de autenticação." }, { status: 502 });
  }

  const response = NextResponse.json({ user: result.user });
  response.cookies.set(ACCESS_TOKEN_COOKIE, result.token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: result.expiresIn,
  });
  return response;
}
