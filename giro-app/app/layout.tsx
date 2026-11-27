import "./globals.css";
import React from "react";

export const metadata = {
  title: "Giro | Seu dinheiro, no seu ritmo",
  description: "Conheça os serviços financeiros simulados do Giro e organize suas finanças.",
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