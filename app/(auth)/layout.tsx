export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="auth-bg relative min-h-screen overflow-hidden text-slate-200">

      <div className="pointer-events-none fixed inset-0 overflow-hidden z-0">
        <div className="orb orb-1" />
        <div className="orb orb-2" />
        <div className="orb orb-3" />
      </div>

      <main className="relative z-10 flex min-h-screen flex-col md:flex-row">

        <section className="relative hidden overflow-hidden p-12 md:flex md:w-3/5 md:flex-col md:justify-center md:pl-20">
          <div className="relative z-10 max-w-2xl">
            <h1 className="kinetic-text mb-6 text-7xl font-black leading-tight tracking-tighter font-display">
              VieShop
              <br />
              Quản Lý
            </h1>
            <p className="mb-8 text-xl leading-relaxed text-slate-400">
              Quản lý tài khoản Google Family và ChatGPT Workspace
            </p>
          </div>

          <div className="float-card float-card-1 bg-red-500/10 text-red-300">
            <img
              src="/youtubelogo.png"
              alt="YouTube"
              className="h-6 w-6 object-contain"
            />
            YouTube
          </div>
          <div className="float-card float-card-2 bg-blue-500/10 text-blue-300">
            <img src="/geminilogo.png" alt="Gemini" className="h-6 w-6 object-contain" />
            Gemini
          </div>
          <div className="float-card float-card-3 bg-emerald-500/10 text-emerald-300">
            <img
              src="/googleonelogo.png"
              alt="Google One"
              className="h-6 w-6 object-contain"
            />
            Google One
          </div>
          <div className="float-card float-card-4 bg-yellow-500/10 text-yellow-300">
            <img
              src="/googledrivelogo.png"
              alt="Drive"
              className="h-6 w-6 object-contain"
            />
            Drive
          </div>
          <div className="float-card float-card-5 bg-slate-500/10 text-slate-200">
            <img
              src="/chatgptlogo.png"
              alt="ChatGPT"
              className="h-6 w-6 object-contain"
            />
            ChatGPT
          </div>
          <div className="float-card float-card-6 bg-violet-500/10 text-violet-300">
            <img src="/capcutlogo.png" alt="CapCut" className="h-6 w-6 object-contain" />
            CapCut
          </div>
        </section>

        <section className="relative flex flex-1 items-center justify-center p-4 sm:p-6">
          {children}
        </section>
      </main>
    </div>
  );
}
