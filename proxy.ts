import { createHmac, timingSafeEqual } from "node:crypto";
import { NextRequest, NextResponse } from "next/server";

const TOKEN_COOKIE = "giro_access_token";

function hasValidToken(token: string | undefined): boolean {
  const secretBase64 = process.env.GIRO_JWT_SECRET_BASE64;
  if (!secretBase64) {
    throw new Error("GIRO_JWT_SECRET_BASE64 is required by the authentication proxy.");
  }
  const secret = Buffer.from(secretBase64, "base64");
  if (secret.length < 32) {
    throw new Error("GIRO_JWT_SECRET_BASE64 must contain at least 32 bytes.");
  }
  if (!token) return false;

  try {
    const [headerPart, payloadPart, signaturePart, extraPart] = token.split(".");
    if (!headerPart || !payloadPart || !signaturePart || extraPart) return false;
    const header = JSON.parse(Buffer.from(headerPart, "base64url").toString("utf8")) as { alg?: string };
    if (header.alg !== "HS256") return false;

    const expected = createHmac("sha256", secret)
      .update(`${headerPart}.${payloadPart}`)
      .digest();
    const actual = Buffer.from(signaturePart, "base64url");
    if (actual.length !== expected.length || !timingSafeEqual(actual, expected)) return false;

    const claims = JSON.parse(Buffer.from(payloadPart, "base64url").toString("utf8")) as {
      iss?: string;
      aud?: string | string[];
      sub?: string;
      exp?: number;
    };
    const audience = Array.isArray(claims.aud) ? claims.aud : [claims.aud];
    return claims.iss === "giro-app"
      && audience.includes("giro-api")
      && Boolean(claims.sub)
      && typeof claims.exp === "number"
      && claims.exp > Math.floor(Date.now() / 1000);
  } catch {
    return false;
  }
}

export function proxy(request: NextRequest) {
  const token = request.cookies.get(TOKEN_COOKIE)?.value;
  if (hasValidToken(token)) {
    return NextResponse.next();
  }

  const response = NextResponse.redirect(new URL("/", request.url));
  if (token) response.cookies.delete(TOKEN_COOKIE);
  return response;
}

export const config = {
  matcher: ["/dashboard/:path*"],
};
