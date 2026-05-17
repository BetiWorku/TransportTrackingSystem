import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useLocation, Navigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Bus as BusIcon,
  MessageSquare,
  MapPin,
  Settings as SettingsIcon,
  LogOut,
  Bell,
  Search,
  Menu,
  X,
  Map as MapIcon,
  PlusCircle,
  FileText,
  Users
} from 'lucide-react';

import { db } from './firebase';
import { collection, query, where, onSnapshot, doc, setDoc, serverTimestamp, getDocs, writeBatch } from 'firebase/firestore';
import AdminLogin from './components/AdminLogin';
import {
  DashboardHome,
  BusManagement,
  ComplaintsManagement,
  RouteManagement,
  NewsManagement,
  LiveMap,
  TerminalsManagement,
  RegisterBus,
  SettingsPage as Settings,
  UsersList
} from './components/DashboardComponents';

const SidebarItem = ({ icon: Icon, label, path, active, badge, badgeColor = "bg-red-500 animate-pulse", onClick }) => (
  <Link
    to={path}
    onClick={onClick}
    className={`flex items-center justify-between px-4 py-3 rounded-lg transition-all duration-200 ${active
        ? 'bg-primary-600 text-white shadow-lg shadow-primary-200'
        : 'text-gray-600 hover:bg-primary-50 hover:text-primary-600'
      }`}
  >
    <div className="flex items-center space-x-3">
      <Icon size={20} />
      <span className="font-medium whitespace-nowrap overflow-hidden">{label}</span>
    </div>
    {badge > 0 && (
      <span className={`${badgeColor} text-white text-[10px] font-bold px-2 py-0.5 rounded-full`}>
        {badge}
      </span>
    )}
  </Link>
);

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isSidebarOpen, setSidebarOpen] = useState(true);
  const location = useLocation();

  const [pendingComplaints, setPendingComplaints] = useState(0);
  const [totalNews, setTotalNews] = useState(0);
  const [movementNotifications, setMovementNotifications] = useState([]);
  const [unreadMovements, setUnreadMovements] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    const q = query(collection(db, "complaints"), where("status", "==", "pending"));
    return onSnapshot(q, (snap) => setPendingComplaints(snap.size));
  }, []);

  useEffect(() => {
    return onSnapshot(collection(db, "news"), (snap) => setTotalNews(snap.size));
  }, []);

  const [totalBuses, setTotalBuses] = useState(0);

  useEffect(() => {
    return onSnapshot(collection(db, "buses"), (snap) => setTotalBuses(snap.size));
  }, []);

  const [totalUsers, setTotalUsers] = useState(0);

  useEffect(() => {
    return onSnapshot(collection(db, "users"), (snap) => setTotalUsers(snap.size));
  }, []);

  useEffect(() => {
    const unsub = onSnapshot(collection(db, "buses"), (snap) => {
      snap.docChanges().forEach(async (change) => {
        if (change.type === "modified" || change.type === "added") {
          const bus = change.doc.data();
          if (!bus.lastStop && !bus.status) return;

          const msg = bus.status === "Active" && bus.lastStop
            ? `Bus ${bus.busId || bus.busNumber} reached station: ${bus.lastStop}`
            : `Bus ${bus.busId || bus.busNumber} status is ${bus.status}`;

          // Auto-Post to News Collection
          const newsId = `NEWS_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;
          await setDoc(doc(db, "news", newsId), {
            title: bus.lastStop ? "Bus Arriving Soon!" : "Fleet Update",
            content: msg,
            newsId,
            type: 'alert',
            timestamp: serverTimestamp()
          });

          setUnreadMovements(prev => prev + 1);
        }
      });
    });
    return () => unsub();
  }, []);

  useEffect(() => {
    const unsub = onSnapshot(collection(db, "route_alerts"), (snap) => {
      snap.docChanges().forEach((change) => {
        if (change.type === "added") {
          const alert = change.doc.data();
          const newNotif = {
            id: change.doc.id,
            title: "User Trip Planned",
            message: `User ${alert.userEmail} searching: ${alert.from} ➔ ${alert.to}`,
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            type: 'alert'
          };
          setMovementNotifications(prev => {
            if (prev.find(n => n.id === newNotif.id)) return prev;
            return [newNotif, ...prev].slice(0, 15);
          });
          setUnreadMovements(prev => prev + 1);
        }
      });
    });
    return () => unsub();
  }, []);

  if (!isAuthenticated) {
    return <AdminLogin onLogin={() => setIsAuthenticated(true)} busCount={totalBuses} />;
  }

  return (
    <div className="flex h-screen bg-gray-50 font-sans overflow-hidden relative">
      {/* Mobile Sidebar Backdrop */}
      {isSidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`
        ${isSidebarOpen ? 'w-64 translate-x-0' : 'w-20 lg:w-20 -translate-x-full lg:translate-x-0'} 
        fixed lg:relative h-full bg-white border-r border-gray-200 transition-all duration-300 ease-in-out flex flex-col z-50
      `}>
        <div className="p-6 flex items-center justify-between">
          {isSidebarOpen && (
            <span className="text-xl font-bold text-primary-600 flex items-center gap-2">
              <BusIcon size={24} /> MyTransport
            </span>
          )}
          <button
            onClick={(e) => { e.stopPropagation(); setSidebarOpen(!isSidebarOpen); }}
            className="p-2 hover:bg-gray-100 rounded-lg text-gray-500"
          >
            {isSidebarOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>

        <nav className="flex-1 px-4 space-y-1 mt-4 overflow-y-auto">
          <SidebarItem
            icon={LayoutDashboard}
            label={isSidebarOpen ? "Fleet Dashboard" : ""}
            path="/"
            active={location.pathname === "/"}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <SidebarItem
            icon={PlusCircle}
            label={isSidebarOpen ? "Register New Bus" : ""}
            path="/register-bus"
            active={location.pathname === "/register-bus"}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <SidebarItem
            icon={MapIcon}
            label={isSidebarOpen ? "Live Map" : ""}
            path="/live-map"
            active={location.pathname === "/live-map"}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <SidebarItem
            icon={MapPin}
            label={isSidebarOpen ? "Manage Terminals" : ""}
            path="/terminals"
            active={location.pathname === "/terminals"}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <SidebarItem
            icon={FileText}
            label={isSidebarOpen ? "Manage Routes" : ""}
            path="/routes"
            active={location.pathname === "/routes"}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <SidebarItem
            icon={Bell}
            label={isSidebarOpen ? "Manage News" : ""}
            path="/news"
            active={location.pathname === "/news"}
            badge={totalNews}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <SidebarItem
            icon={MessageSquare}
            label={isSidebarOpen ? "User Complaints" : ""}
            path="/complaints"
            active={location.pathname === "/complaints"}
            badge={pendingComplaints}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <SidebarItem
            icon={Users}
            label={isSidebarOpen ? "Registered Users" : ""}
            path="/users"
            active={location.pathname === "/users"}
            badge={totalUsers}
            badgeColor="bg-blue-600 animate-pulse"
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
        </nav>

        <div className="p-4 border-t border-gray-100">
          <SidebarItem
            icon={SettingsIcon}
            label={isSidebarOpen ? "Settings" : ""}
            path="/settings"
            active={location.pathname === "/settings"}
            onClick={() => window.innerWidth < 1024 && setSidebarOpen(false)}
          />
          <button
            onClick={() => setIsAuthenticated(false)}
            className="flex items-center space-x-3 px-4 py-3 rounded-lg text-red-500 hover:bg-red-50 w-full mt-2 transition-colors"
          >
            <LogOut size={20} />
            {isSidebarOpen && <span className="font-medium">Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-4 lg:px-8 z-20">
          <div className="flex items-center gap-4">
            <button
              onClick={() => setSidebarOpen(!isSidebarOpen)}
              className="lg:hidden p-2 hover:bg-gray-100 rounded-lg"
            >
              <LayoutDashboard size={24} className="text-primary-600" />
            </button>
            <div className="flex items-center bg-gray-100 px-3 lg:px-4 py-2 rounded-xl w-40 sm:w-64 lg:w-96">
              <Search size={18} className="text-gray-400" />
              <input
                type="text"
                placeholder="Search..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="bg-transparent border-none focus:ring-0 text-sm ml-2 w-full"
              />
            </div>
          </div>

          <div className="flex items-center space-x-6">
            <div className="flex items-center space-x-4 border-r border-gray-100 pr-6">
              <div className="relative">
                <button
                  onClick={(e) => { e.stopPropagation(); setShowNotifications(!showNotifications); setUnreadMovements(0); }}
                  className="p-2 text-gray-400 hover:bg-gray-50 rounded-full transition-colors relative"
                >
                  <Bell size={20} />
                  {unreadMovements > 0 && (
                    <span className="absolute top-0 right-0 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center animate-bounce">
                      {unreadMovements}
                    </span>
                  )}
                </button>

                {/* Notification Dropdown */}
                {showNotifications && (
                  <div className="absolute right-0 mt-2 w-80 bg-white rounded-2xl shadow-2xl border border-gray-100 overflow-hidden z-50 animate-in fade-in slide-in-from-top-2 duration-200" onClick={(e) => e.stopPropagation()}>
                    <div className="p-4 border-b border-gray-50 flex justify-between items-center bg-gray-50/50">
                      <h3 className="font-bold text-gray-800 text-sm">Live Movements</h3>
                      <button onClick={() => setMovementNotifications([])} className="text-[10px] text-primary-600 font-bold hover:underline">Clear All</button>
                    </div>
                    <div className="max-h-96 overflow-y-auto">
                      {movementNotifications.length > 0 ? (
                        movementNotifications.map(n => (
                          <div key={n.id} className="p-4 border-b border-gray-50 hover:bg-gray-50 transition-colors">
                            <div className="flex justify-between items-start mb-1">
                              <p className={`text-xs font-bold ${n.type === 'alert' ? 'text-green-600' : 'text-primary-700'}`}>
                                {n.title}
                              </p>
                              <p className="text-[10px] text-gray-400">{n.time}</p>
                            </div>
                            <p className="text-xs text-gray-600 leading-tight">{n.message}</p>
                          </div>
                        ))
                      ) : (
                        <div className="p-8 text-center">
                          <MapPin className="mx-auto text-gray-200 mb-2" size={32} />
                          <p className="text-xs text-gray-400 italic">No movements recorded recently.</p>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
              <Link to="/complaints" className="p-2 text-gray-400 hover:bg-gray-50 rounded-full transition-colors relative">
                <MessageSquare size={20} />
                {pendingComplaints > 0 && (
                  <span className="absolute top-0 right-0 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center animate-bounce">
                    {pendingComplaints}
                  </span>
                )}
              </Link>
            </div>

            <div className="flex items-center space-x-3">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-semibold text-gray-800">Admin Panel</p>
                <p className="text-xs text-gray-500">Super Admin</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 font-bold border-2 border-primary-200">
                A
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 overflow-y-auto bg-gray-50">
          <Routes>
            <Route path="/" element={<BusManagement searchQuery={searchQuery} />} />
            <Route path="/register-bus" element={<RegisterBus />} />
            <Route path="/live-map" element={<LiveMap />} />
            <Route path="/terminals" element={<TerminalsManagement searchQuery={searchQuery} />} />
            <Route path="/routes" element={<RouteManagement searchQuery={searchQuery} />} />
            <Route path="/news" element={<NewsManagement searchQuery={searchQuery} />} />
            <Route path="/complaints" element={<ComplaintsManagement searchQuery={searchQuery} />} />
            <Route path="/users" element={<UsersList searchQuery={searchQuery} />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

export default App;
