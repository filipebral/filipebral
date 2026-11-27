"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

const services = [
  { icon: "↗", title: "Conta e Pix", description: "Experimente saldo, extrato, transferências e agendamentos fictícios.", color: "bg-[#d9f99d]" },
  { icon: "▤", title: "Cartão virtual", description: "Simule compras no crédito e débito, limites e faturas.", color: "bg-[#dcd5ff]" },
  { icon: "▥", title: "Boletos", description: "Organize e simule pagamentos, sem usar códigos de barras reais.", color: "bg-[#ffdfb6]" },
  { icon: "◒", title: "Organização financeira", description: "Acompanhe categorias, orçamentos e metas com saldo fictício.", color: "bg-[#bdebe2]" },
  { icon: "✳", title: "Rateios", description: "Divida despesas em grupo e acompanhe as cotas declaradas.", color: "bg-[#ffcee3]" },
  { icon: "↗", title: "Simuladores", description: "Explore cenários de empréstimos e investimentos, sem contratar.", color: "bg-[#d9e8ff]" },
];

export default function Home() {
  const router = useRouter();
  const [isRegistering, setIsRegistering] = useState(false);
  const [accountType, setAccountType] = useState<"personal" | "business">("personal");
  const [name, setName] = useState("");
  const [cpf, setCpf] = useState("");
  const [password, setPassword] = useState("");
  const [birthDate, setBirthDate] = useState("");
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
        body: JSON.stringify({
          cpf: cpf.replace(/\D/g, ""),
          password,
          ...(isRegistering ? { name, birthDate } : {}),
        }),
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
    <div className="min-h-screen bg-[#f7f8f3] font-sans text-[#15251c] selection:bg-[#c4f65a] selection:text-[#15251c]">
      <header className="bg-[#10251b] text-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-5 px-5 py-5 sm:px-8 lg:px-12">
          <a href="#" aria-label="Giro, início" className="text-[30px] font-black leading-none tracking-[-0.08em]">giro<span className="text-[#c4f65a]">.</span></a>
          <nav aria-label="Navegação principal" className="hidden items-center gap-8 text-sm font-medium text-white/75 md:flex">
            <a href="#servicos" className="transition hover:text-white">Para você</a>
            <a href="#acesso" onClick={() => { setAccountType("business"); setError(null); }} className="transition hover:text-white">Para seu negócio</a>
            <a href="#como-funciona" className="transition hover:text-white">Como funciona</a>
          </nav>
          <a href="#acesso" className="rounded-full border border-white/30 px-5 py-2.5 text-sm font-bold transition hover:bg-white hover:text-[#10251b]">Entrar <span aria-hidden="true">↗</span></a>
        </div>
      </header>

      <main>
        <section className="overflow-hidden bg-[#10251b] text-white">
          <div className="mx-auto grid max-w-7xl items-center gap-12 px-5 pb-16 pt-12 sm:px-8 sm:pb-20 lg:grid-cols-[1fr_0.85fr] lg:gap-20 lg:px-12 lg:pb-24 lg:pt-20">
            <div>
              <span className="inline-flex items-center gap-2 rounded-full border border-[#c4f65a]/30 bg-[#c4f65a]/10 px-4 py-2 text-xs font-bold uppercase tracking-[0.12em] text-[#c4f65a]">
                <span className="h-2 w-2 rounded-full bg-[#c4f65a]" /> Conheça o Giro
              </span>
              <h1 className="mt-7 max-w-[650px] text-5xl font-semibold leading-[1.05] tracking-[-0.055em] sm:text-6xl lg:text-[76px]">Seu dinheiro, <span className="text-[#c4f65a]">no seu ritmo.</span></h1>
              <p className="mt-6 max-w-lg text-base leading-7 text-white/70 sm:text-lg">Uma forma mais simples de organizar suas finanças, planejar o futuro e dividir os momentos. Tudo em um só lugar.</p>
              <div className="mt-9 flex flex-wrap items-center gap-4">
                <a href="#acesso" className="inline-flex items-center gap-10 rounded-full bg-[#c4f65a] px-6 py-4 text-sm font-bold text-[#10251b] transition hover:bg-[#d7ff83]">Conhecer o Giro <span aria-hidden="true" className="text-lg">↗</span></a>
                <a href="#servicos" className="px-3 py-3 text-sm font-semibold text-white/85 underline decoration-white/35 underline-offset-8 hover:text-white">Explorar serviços</a>
              </div>
              <p className="mt-8 max-w-lg text-xs leading-5 text-white/50">Ambiente de simulação: nenhum dinheiro real é movimentado e nenhum provedor financeiro está conectado.</p>
            </div>

            <div aria-label="Ilustração de conta e cartão simulados" className="relative mx-auto w-full max-w-[470px] py-6 sm:py-10">
              <div className="absolute inset-4 rounded-full bg-[#bdf058]/15 blur-[75px]" />
              <div className="relative ml-auto w-[86%] rotate-[4deg] rounded-[30px] bg-[#c4f65a] p-6 text-[#183125] shadow-[0_30px_90px_rgba(0,0,0,0.3)] sm:p-8">
                <div className="flex items-start justify-between"><span className="text-3xl font-black tracking-[-0.08em]">giro.</span><span className="rounded-full border border-[#183125]/25 px-3 py-1 text-[10px] font-bold uppercase tracking-wider">Simulado</span></div>
                <div className="mt-16 sm:mt-24">
                  <p className="text-xs font-semibold uppercase tracking-[0.15em] text-[#183125]/65">Cartão virtual</p>
                  <p className="mt-2 text-2xl font-semibold tracking-tight">Seu dia, do seu jeito.</p>
                </div>
              </div>
              <div className="relative -mt-10 w-[78%] rounded-3xl border border-white/10 bg-[#f7f8f3] p-5 text-[#15251c] shadow-[0_20px_60px_rgba(0,0,0,0.25)] sm:p-6">
                <div className="flex items-center justify-between"><span className="text-xs font-bold uppercase tracking-[0.12em] text-[#718074]">Conta simulada</span><span className="text-lg">✳</span></div>
                <p className="mt-5 text-sm text-[#68776d]">Um espaço para planejar</p>
                <p className="mt-1 text-xl font-bold tracking-tight sm:text-2xl">Seu futuro começa aqui.</p>
                <div className="mt-5 h-2 rounded-full bg-[#e1e9d8]"><div className="h-full w-2/3 rounded-full bg-[#91c933]" /></div>
              </div>
            </div>
          </div>
        </section>

        <section id="servicos" className="mx-auto max-w-7xl px-5 py-20 sm:px-8 lg:px-12 lg:py-28">
          <div className="flex flex-col justify-between gap-5 md:flex-row md:items-end">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.17em] text-[#61833a]">Um giro de possibilidades</p>
              <h2 className="mt-4 max-w-xl text-4xl font-semibold leading-[1.08] tracking-[-0.05em] sm:text-5xl">Tudo o que importa, mais perto de você.</h2>
            </div>
            <p className="max-w-sm text-sm leading-6 text-[#6c796e]">Conheça as funcionalidades disponíveis para experimentar no Giro. Por enquanto, os produtos financeiros são simulados.</p>
          </div>
          <div className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {services.map((service) => (
              <article key={service.title} className="group flex min-h-[220px] flex-col rounded-[26px] border border-[#e5e9e0] bg-white p-7 transition hover:-translate-y-1 hover:shadow-[0_16px_40px_rgba(23,49,32,0.08)]">
                <div className="flex items-start justify-between">
                  <span aria-hidden="true" className={`flex h-12 w-12 items-center justify-center rounded-2xl text-2xl font-medium ${service.color}`}>{service.icon}</span>
                  <span className="rounded-full bg-[#f1f5ec] px-3 py-1 text-[10px] font-bold uppercase tracking-wider text-[#65765d]">Simulação</span>
                </div>
                <h3 className="mt-6 text-xl font-bold tracking-[-0.03em]">{service.title}</h3>
                <p className="mt-2 text-sm leading-6 text-[#718074]">{service.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section id="como-funciona" className="bg-[#eaf1df]">
          <div className="mx-auto grid max-w-7xl gap-10 px-5 py-16 sm:px-8 lg:grid-cols-2 lg:items-center lg:gap-20 lg:px-12 lg:py-24">
            <div>
              <p className="text-xs font-bold uppercase tracking-[0.17em] text-[#61833a]">Mais clareza, menos complicação</p>
              <h2 className="mt-4 text-4xl font-semibold leading-[1.1] tracking-[-0.05em] sm:text-5xl">Planejar pode ser mais leve.</h2>
              <p className="mt-5 max-w-lg leading-7 text-[#60715f]">Teste ideias, acompanhe metas e entenda seus gastos em um ambiente feito para você experimentar com tranquilidade.</p>
            </div>
            <div className="rounded-[28px] bg-[#10251b] p-8 text-white sm:p-10">
              <span aria-hidden="true" className="text-3xl text-[#c4f65a]">✳</span>
              <h3 className="mt-5 text-2xl font-semibold tracking-tight">Transparência em primeiro lugar.</h3>
              <p className="mt-3 text-sm leading-6 text-white/70">Saldo, Pix, cartões, boletos e calculadoras são fictícios. Não há abertura de conta bancária, contratação ou movimentação de dinheiro real.</p>
            </div>
          </div>
        </section>

        <section id="acesso" className="mx-auto grid max-w-7xl gap-10 px-5 py-20 sm:px-8 lg:grid-cols-2 lg:items-start lg:gap-20 lg:px-12 lg:py-28">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.17em] text-[#61833a]">Seu espaço no Giro</p>
            <h2 className="mt-4 max-w-lg text-4xl font-semibold leading-[1.08] tracking-[-0.05em] sm:text-5xl">Pronto para dar o primeiro passo?</h2>
            <p className="mt-5 max-w-md leading-7 text-[#718074]">Entre com seu CPF ou crie seu acesso para conhecer a experiência simulada do Giro.</p>
            <p className="mt-6 text-sm font-medium text-[#536b50]">✳ &nbsp; Uma experiência para planejar, não para movimentar dinheiro.</p>
          </div>
          <div className="rounded-[28px] border border-[#e5e9e0] bg-white p-6 shadow-[0_15px_60px_rgba(23,49,32,0.06)] sm:p-9">
            <h3 className="text-2xl font-bold tracking-tight">Acesse o Giro</h3>
            <p className="mt-1 text-sm text-[#718074]">Escolha o tipo de acesso.</p>
            <div aria-label="Tipo de cliente" className="mt-6 grid grid-cols-2 gap-1 rounded-2xl bg-[#f1f4ee] p-1">
              <button type="button" aria-pressed={accountType === "personal"} onClick={() => { setAccountType("personal"); setError(null); }} className={`rounded-xl px-2 py-3 text-sm font-bold transition ${accountType === "personal" ? "bg-white text-[#173225] shadow-sm" : "text-[#718074] hover:text-[#173225]"}`}>Pessoa física</button>
              <button type="button" aria-pressed={accountType === "business"} onClick={() => { setAccountType("business"); setError(null); }} className={`rounded-xl px-2 py-3 text-sm font-bold transition ${accountType === "business" ? "bg-white text-[#173225] shadow-sm" : "text-[#718074] hover:text-[#173225]"}`}>Comercial</button>
            </div>
            {accountType === "business" ? (
              <div role="status" className="mt-6 rounded-2xl bg-[#f5f8f1] p-5">
                <h4 className="font-bold">Acesso comercial ainda não disponível</h4>
                <p className="mt-2 text-sm leading-6 text-[#718074]">O cadastro e o login para empresas aguardam a escolha de um serviço para verificar o CNPJ e os poderes do representante. Por enquanto, o acesso ao ambiente simulado é individual, com CPF.</p>
                <button type="button" onClick={() => setAccountType("personal")} className="mt-4 text-sm font-bold text-[#345e26] underline underline-offset-4">Ir para acesso pessoal</button>
              </div>
            ) : (
              <>
                <h4 className="mt-6 text-lg font-bold">{isRegistering ? "Crie seu acesso" : "Entre na sua conta"}</h4>
                {isRegistering && <p className="mt-3 rounded-xl bg-[#f5f8f1] p-3 text-sm text-[#536b50]">Novos cadastros estão indisponíveis até a configuração da consulta cadastral. O acesso de clientes já cadastrados continua disponível.</p>}
                {error && <div role="alert" className="mt-4 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}
                <form onSubmit={handleSubmit} className="mt-5 space-y-4">
                  {isRegistering && <div><label htmlFor="name" className="mb-1.5 block text-sm font-semibold">Nome</label><input id="name" type="text" required minLength={2} maxLength={100} autoComplete="name" value={name} onChange={(event) => setName(event.target.value)} className="w-full rounded-xl border border-[#dce4d9] bg-[#fafcf9] px-4 py-3 text-sm outline-none focus:border-[#527f35] focus:ring-2 focus:ring-[#c4f65a]" /></div>}
                  <div>
                    <label htmlFor="cpf" className="mb-1.5 block text-sm font-semibold">CPF</label>
                    <input id="cpf" type="text" inputMode="numeric" autoComplete="off" required maxLength={14} placeholder="000.000.000-00" value={cpf} onChange={(event) => {
                      const digits = event.target.value.replace(/\D/g, "").slice(0, 11);
                      setCpf(digits.replace(/^(\d{3})(\d)/, "$1.$2").replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3").replace(/^(\d{3})\.(\d{3})\.(\d{3})(\d)/, "$1.$2.$3-$4"));
                    }} className="w-full rounded-xl border border-[#dce4d9] bg-[#fafcf9] px-4 py-3 text-sm outline-none focus:border-[#527f35] focus:ring-2 focus:ring-[#c4f65a]" />
                  </div>
                  <div>
                    {isRegistering && <div className="mb-4"><label htmlFor="birthDate" className="mb-1.5 block text-sm font-semibold">Data de nascimento</label><input id="birthDate" type="date" required autoComplete="bday" value={birthDate} onChange={(event) => setBirthDate(event.target.value)} className="w-full rounded-xl border border-[#dce4d9] bg-[#fafcf9] px-4 py-3 text-sm outline-none focus:border-[#527f35] focus:ring-2 focus:ring-[#c4f65a]" /></div>}
                    <label htmlFor="password" className="mb-1.5 block text-sm font-semibold">Senha</label>
                    <input id="password" type="password" required minLength={isRegistering ? 12 : undefined} maxLength={72} autoComplete={isRegistering ? "new-password" : "current-password"} value={password} onChange={(event) => setPassword(event.target.value)} className="w-full rounded-xl border border-[#dce4d9] bg-[#fafcf9] px-4 py-3 text-sm outline-none focus:border-[#527f35] focus:ring-2 focus:ring-[#c4f65a]" />
                    {isRegistering && <p className="mt-1 text-xs text-[#718074]">Use no mínimo 12 caracteres.</p>}
                  </div>
                  <button type="submit" disabled={loading} className="w-full rounded-xl bg-[#173b28] px-4 py-3.5 text-sm font-bold text-white transition hover:bg-[#28583a] disabled:opacity-50">{loading ? "Aguarde..." : isRegistering ? "Criar acesso" : "Entrar"} <span aria-hidden="true">→</span></button>
                </form>
                <p className="mt-5 text-center text-sm text-[#718074]">{isRegistering ? "Já possui um acesso?" : "Ainda não tem acesso?"}{" "}<button type="button" onClick={() => { setError(null); setIsRegistering(!isRegistering); }} className="font-bold text-[#345e26] underline underline-offset-4">{isRegistering ? "Entrar" : "Criar acesso"}</button></p>
              </>
            )}
          </div>
        </section>
      </main>
      <footer className="bg-[#10251b] text-white">
        <div className="mx-auto flex max-w-7xl flex-col gap-5 px-5 py-10 sm:flex-row sm:items-end sm:justify-between sm:px-8 lg:px-12">
          <div><span className="text-3xl font-black tracking-[-0.08em]">giro<span className="text-[#c4f65a]">.</span></span><p className="mt-2 text-sm text-white/60">Seu dinheiro, no seu ritmo.</p></div>
          <p className="max-w-md text-xs leading-5 text-white/50">Giro é uma experiência simulada. Serviços financeiros reais dependem de integração com provedor e não estão disponíveis.</p>
        </div>
      </footer>
    </div>
  );
}
