"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

export default function Home() {
  const router = useRouter();
  const [isRegistering, setIsRegistering] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/auth/${isRegistering ? "register" : "login"}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, ...(isRegistering ? { name } : {}) }),
      });
      const result = await response.json();
      if (!response.ok) {
        throw new Error(result.message || "Não foi possível autenticar.");
      }
      router.replace("/dashboard");
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Não foi possível autenticar.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#09090b] text-zinc-100 flex flex-col justify-between selection:bg-[#00FF66] selection:text-black">
      <header className="w-full max-w-6xl mx-auto px-6 py-6 flex items-center">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-xl bg-[#00FF66]/10 border border-[#00FF66]/30 flex items-center justify-center text-[#00FF66] font-extrabold text-sm">G</div>
          <span className="text-lg font-bold tracking-tight text-white">giro<span className="text-[#00FF66]">.</span></span>
        </div>
      </header>

      <main className="w-full max-w-md mx-auto px-4 py-8 flex flex-col items-center justify-center flex-1">
        <div className="w-full bg-[#121214] border border-white/10 rounded-3xl p-8 shadow-2xl">
          <div className="mb-6">
            <span className="text-xs font-semibold uppercase tracking-wider text-[#00FF66]">Acesso seguro</span>
            <h1 className="text-2xl font-extrabold text-white tracking-tight mt-1">
              {isRegistering ? "Crie sua conta Giro" : "Entre na sua conta"}
            </h1>
            <p className="text-xs text-zinc-400 mt-1">Use suas credenciais para acessar o Giro.</p>
          </div>

          {error && (
            <div role="alert" className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-2xl text-red-400 text-xs">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {isRegistering && (
              <div>
                <label htmlFor="name" className="block text-xs font-medium uppercase tracking-wider text-zinc-400 mb-1.5">Nome</label>
                <input id="name" type="text" required minLength={2} maxLength={100} autoComplete="name" value={name} onChange={(event) => setName(event.target.value)} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3.5 text-white focus:outline-none focus:border-[#00FF66] text-sm" />
              </div>
            )}
            <div>
              <label htmlFor="email" className="block text-xs font-medium uppercase tracking-wider text-zinc-400 mb-1.5">E-mail</label>
              <input id="email" type="email" required maxLength={254} autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3.5 text-white focus:outline-none focus:border-[#00FF66] text-sm" />
            </div>
            <div>
              <label htmlFor="password" className="block text-xs font-medium uppercase tracking-wider text-zinc-400 mb-1.5">Senha</label>
              <input id="password" type="password" required minLength={isRegistering ? 12 : undefined} maxLength={72} autoComplete={isRegistering ? "new-password" : "current-password"} value={password} onChange={(event) => setPassword(event.target.value)} className="w-full bg-[#18181b] border border-white/10 rounded-2xl px-4 py-3.5 text-white focus:outline-none focus:border-[#00FF66] text-sm" />
              {isRegistering && <p className="mt-1 text-xs text-zinc-500">Use no mínimo 12 caracteres.</p>}
            </div>
            <button type="submit" disabled={loading} className="w-full mt-2 bg-[#00FF66] hover:bg-[#00cc52] text-black py-3.5 px-4 rounded-2xl text-xs font-extrabold disabled:opacity-50">
              {loading ? "Aguarde..." : isRegistering ? "Criar conta" : "Entrar"}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-white/5 text-center text-xs text-zinc-500">
            {isRegistering ? "Já possui uma conta?" : "Ainda não é cliente?"}{" "}
            <button type="button" onClick={() => { setError(null); setIsRegistering(!isRegistering); }} className="text-[#00FF66] font-semibold hover:underline">
              {isRegistering ? "Entrar" : "Criar conta"}
            </button>
          </div>
        </div>
      </main>

      <footer className="w-full max-w-6xl mx-auto px-6 py-6 border-t border-white/5 flex justify-between items-center text-xs text-zinc-500">
        <div>© 2026 Giro Pagamentos S.A.</div>
        <span>Autenticação protegida</span>
      </footer>
    </div>
  );
}
