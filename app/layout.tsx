import "./globals.css";
import React from "react";

export const metadata = {
  title: "Giro | O banco que acompanha o seu ritmo",
  description: "Acesso antecipado ao banco Giro.",
};

//  Código Corrigido: Desestruturando { children } diretamente
export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body>
        {children}
      </body>
    </html>
  );
}