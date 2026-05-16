import React, { useState } from 'react';
import { Bus, Lock, Mail, Eye, EyeOff, ArrowRight, Shield, BarChart3, MapPin, Activity } from 'lucide-react';

const AdminLogin = ({ onLogin, busCount }) => {
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    onLogin();
  };

  return (
    <div className="min-h-screen bg-[#0f0c29] flex items-center justify-center p-6 lg:p-12 relative overflow-hidden font-sans">
      {/* Premium Background Gradients */}
      <div className="absolute inset-0 bg-gradient-to-br from-[#1a1a2e] via-[#16213e] to-[#0f3460] z-0" />
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-purple-600/20 blur-[150px] rounded-full z-0" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-blue-600/20 blur-[150px] rounded-full z-0" />

      {/* Subtle Grid Pattern */}
      <div className="absolute inset-0 z-0 opacity-20" style={{ backgroundImage: 'radial-gradient(#4f46e5 1px, transparent 1px)', backgroundSize: '40px 40px' }}></div>

      <div className="max-w-7xl w-full grid lg:grid-cols-2 gap-16 items-center z-10 relative">

        {/* Left Side: Enterprise Content */}
        <div className="hidden lg:flex flex-col text-left space-y-5 ml-24 animate-in slide-in-from-left duration-700">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 bg-white/10 border border-white/20 rounded-full text-white/80 text-xs font-semibold backdrop-blur-md">
            <span className="w-1.5 h-1.5 bg-green-400 rounded-full animate-pulse shadow-[0_0_8px_#4ade80]"></span>
            Live fleet - {busCount} vehicles online
          </div>

          <div className="space-y-3">
            <h1 className="text-4xl font-semibold text-white leading-tight">
              Move smarter.<br />
              <span className="bg-gradient-to-r from-purple-400 via-pink-400 to-blue-400 bg-clip-text text-transparent">Track everything.</span>
            </h1>
            <p className="text-white/50 text-sm max-w-sm font-medium leading-relaxed">
              Real-time visibility across your transport network — routes, drivers, and deliveries in one command center.
            </p>
          </div>

          <div className="space-y-3 max-w-xs">
            <FeatureCard icon={<MapPin size={18} />} title="Live GPS tracking" desc="Sub-second updates" />
            <FeatureCard icon={<BarChart3 size={18} />} title="Fleet analytics" desc="Insight that scales" />
            <FeatureCard icon={<Shield size={18} />} title="Enterprise security" desc="SOC 2 · ISO 27001" />
          </div>
        </div>

        {/* Right Side: Ultra-Compact Login Card */}
        <div className="flex justify-center lg:justify-end mr-24 animate-in slide-in-from-right duration-700">
          <div className="bg-white w-full max-w-[380px] rounded-[30px] shadow-[0_25px_60px_rgba(0,0,0,0.4)] overflow-hidden border border-white/20">
            <div className="p-6 pb-0 flex flex-col items-center">
              <div className="relative group">
                <div className="absolute inset-0 bg-primary-500 blur-xl opacity-20 group-hover:opacity-40 transition-opacity"></div>
                <div className="relative w-12 h-12 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-xl flex items-center justify-center text-white shadow-lg mb-4 transform group-hover:scale-105 transition-transform duration-300">
                  <Bus size={24} />
                </div>
              </div>
              
              <h1 className="text-2xl font-bold text-gray-800 tracking-tight">Admin Portal</h1>
              <p className="text-gray-400 mt-1 text-center text-[10px] font-medium uppercase tracking-widest">Login to system</p>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-3">
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-gray-400 ml-1 uppercase tracking-widest">Email</label>
                <div className="relative group">
                  <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300 group-focus-within:text-primary-500 transition-colors" size={14} />
                  <input 
                    type="email" 
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="admin@mytransport.com"
                    className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-100 rounded-xl focus:ring-4 focus:ring-primary-500/10 focus:bg-white focus:border-primary-500 outline-none transition-all font-medium text-gray-700 text-sm"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-bold text-gray-400 ml-1 uppercase tracking-widest">Password</label>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300 group-focus-within:text-primary-500 transition-colors" size={14} />
                  <input 
                    type={showPassword ? "text" : "password"} 
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-10 pr-12 py-2.5 bg-gray-50 border border-gray-100 rounded-xl focus:ring-4 focus:ring-primary-500/10 focus:bg-white focus:border-primary-500 outline-none transition-all font-medium text-gray-700 text-sm"
                  />
                  <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-600">
                    {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-between px-1">
                <label className="flex items-center gap-2 cursor-pointer group">
                  <input type="checkbox" className="peer sr-only" />
                  <div className="w-3.5 h-3.5 bg-gray-100 border border-gray-200 rounded peer-checked:bg-primary-500 transition-all"></div>
                  <span className="text-[10px] font-bold text-gray-500 group-hover:text-gray-700 transition-colors">Remember me</span>
                </label>
                <button type="button" className="text-[10px] font-bold text-primary-600 hover:underline uppercase tracking-tighter">Forgot?</button>
              </div>

              <button 
                type="submit"
                className="w-full bg-gradient-to-r from-primary-600 via-indigo-600 to-blue-600 text-white py-3 rounded-xl font-bold text-sm flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-[0.98] transition-all shadow-lg group mt-2"
              >
                Login
                <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform" />
              </button>
            </form>

            <div className="p-4 text-center bg-gray-50/50 border-t border-gray-100">
              <p className="text-[9px] text-gray-400 font-bold uppercase tracking-widest">
                © 2026 TRANSPORT SYSTEM
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const FeatureCard = ({ icon, title, desc }) => (
  <div className="flex items-center gap-4 p-4 bg-white/5 border border-white/10 rounded-2xl backdrop-blur-sm hover:bg-white/10 transition-colors group">
    <div className="w-12 h-12 bg-gradient-to-br from-primary-500 to-blue-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-primary-500/20 group-hover:scale-110 transition-transform">
      {icon}
    </div>
    <div>
      <h3 className="font-bold text-white text-lg">{title}</h3>
      <p className="text-white/40 text-xs font-medium uppercase tracking-widest">{desc}</p>
    </div>
  </div>
);

export default AdminLogin;
