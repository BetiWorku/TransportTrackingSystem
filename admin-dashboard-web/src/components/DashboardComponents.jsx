import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Bus as BusIcon,
  MessageSquare,
  MapPin,
  Settings,
  LogOut,
  Bell,
  Search,
  Users,
  AlertTriangle,
  CheckCircle,
  TrendingUp,
  Plus,
  Edit,
  Trash2,
  Map as MapIcon,
  ChevronRight,
  Send,
  X,
  Clock,
  RefreshCw
} from 'lucide-react';
import { getAuth, updatePassword } from 'firebase/auth';
import { db } from '../firebase';
import {
  collection, onSnapshot, query, doc, deleteDoc, updateDoc, addDoc, serverTimestamp, orderBy, where, setDoc, getDocs, writeBatch
} from 'firebase/firestore';

// --- HELPERS ---
const StatCard = ({ icon: Icon, label, value, color }) => (
  <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow text-left">
    <div className={`p-3 rounded-xl w-fit ${color}`}><Icon size={24} className="text-white" /></div>
    <div className="mt-4">
      <h3 className="text-gray-500 text-sm font-medium">{label}</h3>
      <p className="text-2xl font-bold text-gray-800 mt-1">{value}</p>
    </div>
  </div>
);

// --- COMPONENT: DASHBOARD HOME ---
export const DashboardHome = () => {
  const [stats, setStats] = useState({
    totalBuses: 0,
    activeBuses: 0,
    totalRoutes: 0,
    totalStops: 0,
    pendingComplaints: 0
  });

  useEffect(() => {
    // 1. Buses Stats
    const unsubBuses = onSnapshot(collection(db, "buses"), (snap) => {
      let active = 0;
      snap.forEach(d => { if (d.data().status === "Active") active++; });
      setStats(p => ({ ...p, totalBuses: snap.size, activeBuses: active }));
    });

    // 2. Complaints Stats
    const unsubComp = onSnapshot(collection(db, "complaints"), (snap) => {
      let pending = 0;
      snap.forEach(d => { if (d.data().status === "pending") pending++; });
      setStats(p => ({ ...p, pendingComplaints: pending }));
    });

    // 3. Routes Stats
    const unsubRoutes = onSnapshot(collection(db, "routes"), (snap) => {
      setStats(p => ({ ...p, totalRoutes: snap.size }));
    });

    // 4. Stops (Fermatas) Stats
    const unsubStops = onSnapshot(collection(db, "stops"), (snap) => {
      setStats(p => ({ ...p, totalStops: snap.size }));
    });

    return () => { unsubBuses(); unsubComp(); unsubRoutes(); unsubStops(); };
  }, []);

  return (
    <div className="p-8 animate-in fade-in duration-500 text-left">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Fleet Dashboard</h1>
      <p className="text-gray-500 mb-8">Real-time status of your transport network.</p>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard icon={BusIcon} label="Total Buses" value={stats.totalBuses} color="bg-blue-500" />
        <StatCard icon={CheckCircle} label="Active Vehicles" value={stats.activeBuses} color="bg-green-500" />
        <StatCard icon={MapIcon} label="Active Routes" value={stats.totalRoutes} color="bg-indigo-500" />
        <StatCard icon={MapPin} label="Total Fermatas" value={stats.totalStops} color="bg-purple-500" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <h2 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
            <AlertTriangle className="text-orange-500" size={20} /> Attention Needed
          </h2>
          <div className="flex items-center justify-between p-4 bg-orange-50 rounded-xl border border-orange-100">
            <div>
              <p className="text-orange-900 font-bold text-xl">{stats.pendingComplaints}</p>
              <p className="text-orange-700 text-sm">Pending Complaints</p>
            </div>
            <Link to="/complaints" className="px-4 py-2 bg-orange-500 text-white rounded-lg text-xs font-bold hover:bg-orange-600 transition-colors">View All</Link>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <h2 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
            <Plus className="text-primary-600" size={20} /> Quick Registry
          </h2>
          <div className="flex gap-4">
            <Link to="/register-bus" className="flex-1 p-4 bg-primary-50 rounded-xl border border-primary-100 hover:bg-primary-100 transition-all flex flex-col items-center gap-2 group">
              <Plus className="text-primary-600 group-hover:scale-110 transition-transform" />
              <span className="text-xs font-bold text-primary-900 uppercase tracking-widest text-center">New Bus</span>
            </Link>
            <Link to="/terminals" className="flex-1 p-4 bg-purple-50 rounded-xl border border-purple-100 hover:bg-purple-100 transition-all flex flex-col items-center gap-2 group">
              <MapPin className="text-purple-600 group-hover:scale-110 transition-transform" />
              <span className="text-xs font-bold text-purple-900 uppercase tracking-widest text-center">New Stop</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

// --- COMPONENT: BUS MANAGEMENT ---
export const BusManagement = ({ searchQuery = "" }) => {
  const [buses, setBuses] = useState([]);
  const [editingBus, setEditingBus] = useState(null);
  const [stats, setStats] = useState({ totalBuses: 0, activeBuses: 0, pendingComplaints: 0 });

  useEffect(() => {
    // 1. Listen for Buses
    const unsubBuses = onSnapshot(collection(db, "buses"), (snapshot) => {
      const busData = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      const active = busData.filter(b => b.status === "Active").length;
      setBuses(busData);
      setStats(prev => ({ ...prev, totalBuses: busData.length, activeBuses: active }));
    });

    // 2. Listen for Pending Complaints
    const q = query(collection(db, "complaints"), where("status", "==", "pending"));
    const unsubComp = onSnapshot(q, (snap) => {
      setStats(prev => ({ ...prev, pendingComplaints: snap.size }));
    });

    return () => { unsubBuses(); unsubComp(); };
  }, []);

  const handleDelete = async (id) => {
    if (window.confirm("Are you sure you want to delete this bus?")) {
      await deleteDoc(doc(db, "buses", id));
    }
  };

  const handleUpdateBus = async (e) => {
    e.preventDefault();
    const { id, ...data } = editingBus;
    await updateDoc(doc(db, "buses", id), {
      ...data,
      capacity: parseInt(data.capacity) || 0
    });
    setEditingBus(null);
  };

  const filteredBuses = buses.filter(bus =>
    (bus.busId || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (bus.busNumber || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (bus.driverName || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (bus.terminal || "").toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-8 text-left">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Terminals & Stops</h1>
            <p className="text-gray-500">Manage station names and GPS locations.</p>
          </div>
        </div>
        <div className="flex gap-3">
          <button onClick={async () => {
            if (window.confirm("This will permanently rename all buses in Firebase to match their real IDs (like Wolo-25-01). Proceed?")) {
              const snapshot = await getDocs(collection(db, "buses"));
              for (const docSnap of snapshot.docs) {
                const data = docSnap.data();
                const realId = data.busId;
                if (realId && docSnap.id !== realId) {
                  // 🏗️ Move to new folder, then delete old one
                  await setDoc(doc(db, "buses", realId), { ...data, id: realId });
                  await deleteDoc(doc(db, "buses", docSnap.id));
                  console.log(`Synced: ${docSnap.id} -> ${realId}`);
                }
              }
              alert("Firebase Database has been cleaned and synced!");
            }
          }} className="bg-orange-100 text-orange-600 px-5 py-2.5 rounded-xl font-bold flex items-center gap-2 hover:bg-orange-200 transition-colors">
            <RefreshCw size={20} /> Sync Firebase IDs
          </button>
          <Link to="/register-bus" className="bg-primary-600 text-white px-5 py-2.5 rounded-xl font-bold flex items-center gap-2 shadow-lg shadow-primary-100 hover:scale-105 transition-transform">
            <Plus size={20} /> Add Vehicle
          </Link>
        </div>
      </div>

      {/* Real-time Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 lg:gap-6 mb-8 lg:mb-10">
        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center"><BusIcon size={24} /></div>
          <div>
            <p className="text-xs text-gray-400 font-bold uppercase tracking-widest">Total Fleet</p>
            <p className="text-2xl font-black text-gray-800">{stats.totalBuses}</p>
          </div>
        </div>
        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 bg-green-50 text-green-600 rounded-xl flex items-center justify-center"><CheckCircle size={24} /></div>
          <div>
            <p className="text-xs text-gray-400 font-bold uppercase tracking-widest">Active Now</p>
            <p className="text-2xl font-black text-gray-800">{stats.activeBuses}</p>
          </div>
        </div>
        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 bg-orange-50 text-orange-600 rounded-xl flex items-center justify-center"><AlertTriangle size={24} /></div>
          <div>
            <p className="text-xs text-gray-400 font-bold uppercase tracking-widest">Pending Reports</p>
            <p className="text-2xl font-black text-gray-800">{stats.pendingComplaints}</p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-2xl lg:rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse min-w-[800px] lg:min-w-0">
          <thead className="bg-gray-50 border-b border-gray-100">
            <tr>
              <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Bus Info</th>
              <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Driver</th>
              <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Terminal/Route</th>
              <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase">Status</th>
              <th className="px-6 py-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {filteredBuses.length > 0 ? filteredBuses.map((bus) => (
              <tr key={bus.id} className="hover:bg-gray-50/50 transition-colors">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600">
                      <BusIcon size={20} />
                    </div>
                    <div>
                      <p className="font-bold text-gray-800">{bus.busType || "Standard Bus"}</p>
                      <div className="flex items-center gap-2">
                        <p className={`text-xs ${bus.id !== bus.busId ? 'text-orange-500 font-bold' : 'text-gray-400'}`}>
                          ID: {bus.id}
                        </p>
                        {bus.id !== bus.busId && (
                          <button
                            onClick={async (e) => {
                              e.stopPropagation();
                              if (window.confirm(`Sync Database: Rename folder "${bus.id}" to "${bus.busId}"?`)) {
                                await setDoc(doc(db, "buses", bus.busId), { ...bus, id: bus.busId });
                                await deleteDoc(doc(db, "buses", bus.id));
                              }
                            }}
                            title="Mismatched ID - Click to sync folder name"
                            className="p-1 bg-orange-100 text-orange-600 rounded hover:bg-orange-200 transition-colors"
                          >
                            <RefreshCw size={10} />
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-gray-700 font-medium">
                  <p>{bus.driverName || "Not Assigned"}</p>
                  <p className="text-[10px] text-gray-400">{bus.driverPhone}</p>
                </td>
                <td className="px-6 py-4">
                  <p className="text-sm font-bold text-gray-800">{bus.terminal || "N/A"}</p>
                  <p className="text-xs text-gray-500">Route: {bus.routeId || "General"}</p>
                </td>
                <td className="px-6 py-4">
                  <span className={`px-3 py-1 rounded-full text-[10px] font-bold uppercase ${bus.status === 'Active' ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'}`}>
                    {bus.status || "Inactive"}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  <div className="flex justify-end gap-2">
                    <button onClick={() => setEditingBus(bus)} className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"><Edit size={18} /></button>
                    <button onClick={() => handleDelete(bus.id)} className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"><Trash2 size={18} /></button>
                  </div>
                </td>
              </tr>
            )) : (
              <tr>
                <td colSpan="5" className="px-6 py-20 text-center text-gray-400 italic">No buses match your search.</td>
              </tr>
            )}
          </tbody>
        </table>
        </div>
      </div>

      {/* Edit Modal */}
      {editingBus && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="bg-primary-600 p-6 text-white flex justify-between items-center">
              <div>
                <h2 className="text-xl font-bold">Edit Vehicle</h2>
                <p className="text-primary-100 text-xs">Update bus details for {editingBus.busId}</p>
              </div>
              <button onClick={() => setEditingBus(null)} className="p-2 hover:bg-white/10 rounded-full"><X size={24} /></button>
            </div>
            <form onSubmit={handleUpdateBus} className="p-8 space-y-4 max-h-[70vh] overflow-y-auto text-left">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Bus Number (Line)</label>
                  <input value={editingBus.busNumber} onChange={e => setEditingBus({ ...editingBus, busNumber: e.target.value })} className="w-full mt-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
                </div>
                <div>
                  <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Capacity</label>
                  <input type="number" value={editingBus.capacity} onChange={e => setEditingBus({ ...editingBus, capacity: e.target.value })} className="w-full mt-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Terminal</label>
                  <input value={editingBus.terminal} onChange={e => setEditingBus({ ...editingBus, terminal: e.target.value })} className="w-full mt-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
                </div>
                <div>
                  <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Route ID</label>
                  <input value={editingBus.routeId} onChange={e => setEditingBus({ ...editingBus, routeId: e.target.value })} className="w-full mt-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
                </div>
              </div>
              <div className="space-y-4 pt-2">
                <p className="text-[10px] font-black text-primary-600 uppercase tracking-widest border-b border-gray-50 pb-2">Driver Information</p>
                <input value={editingBus.driverName} onChange={e => setEditingBus({ ...editingBus, driverName: e.target.value })} placeholder="Driver Name" className="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
                <input value={editingBus.driverPhone} onChange={e => setEditingBus({ ...editingBus, driverPhone: e.target.value })} placeholder="Driver Phone" className="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
              </div>
              <div className="pt-2">
                <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Fleet Status</label>
                <select value={editingBus.status} onChange={e => setEditingBus({ ...editingBus, status: e.target.value })} className="w-full mt-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500">
                  <option value="Active">Active</option>
                  <option value="Inactive">Inactive</option>
                  <option value="Maintenance">Maintenance</option>
                </select>
              </div>
              <button type="submit" className="w-full bg-primary-600 text-white py-4 rounded-2xl font-bold hover:bg-primary-700 transition-all shadow-lg shadow-primary-100 mt-4">Save Changes</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// --- COMPONENT: REGISTER BUS ---
export const RegisterBus = () => {
  const [formData, setFormData] = useState({ busId: '', busNumber: '', busType: '', routeId: '', terminal: '', capacity: '', driverName: '', driverPhone: '', status: 'Active' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.busId) return alert("Please enter a Bus ID!");
    await setDoc(doc(db, "buses", formData.busId), { ...formData, capacity: parseInt(formData.capacity) || 0 });
    alert("Bus Registered Successfully with ID: " + formData.busId);
    setFormData({ busId: '', busNumber: '', busType: '', routeId: '', terminal: '', capacity: '', driverName: '', driverPhone: '', status: 'Active' });
  };

  return (
    <div className="min-h-full flex items-center justify-center p-4 lg:p-8 animate-in slide-in-from-bottom-4 duration-500">
      <div className="w-full max-w-2xl text-left">
      <div className="bg-primary-600 p-8 rounded-t-3xl text-white shadow-xl relative overflow-hidden">
        <div className="relative z-10">
          <h1 className="text-3xl font-bold">Fleet Registration</h1>
          <p className="text-primary-100 mt-2 font-medium">Add new vehicles to the active fleet</p>
        </div>
      </div>
      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-b-3xl border border-gray-100 shadow-2xl space-y-6 -mt-4 relative z-20">
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input required value={formData.busId} onChange={e => setFormData({ ...formData, busId: e.target.value })} placeholder="Bus ID (e.g. MEG-44-01)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
            <input required value={formData.busNumber} onChange={e => setFormData({ ...formData, busNumber: e.target.value })} placeholder="Bus Number (Line)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
          </div>
          <input required value={formData.busType} onChange={e => setFormData({ ...formData, busType: e.target.value })} placeholder="Bus Type (e.g. Sheger)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
          <input required value={formData.routeId} onChange={e => setFormData({ ...formData, routeId: e.target.value })} placeholder="Assigned Route ID" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
          <input required value={formData.terminal} onChange={e => setFormData({ ...formData, terminal: e.target.value })} placeholder="Terminal Location" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
          <input required type="number" value={formData.capacity} onChange={e => setFormData({ ...formData, capacity: e.target.value })} placeholder="Passenger Capacity" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
          <div className="pt-4 pb-2 text-left">
            <p className="text-xs font-bold text-primary-600 uppercase tracking-widest ml-1 mb-3">Driver Assignment</p>
            <div className="space-y-4">
              <input required value={formData.driverName} onChange={e => setFormData({ ...formData, driverName: e.target.value })} placeholder="Driver Name" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
              <input required value={formData.driverPhone} onChange={e => setFormData({ ...formData, driverPhone: e.target.value })} placeholder="Driver Phone (09...)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 transition-all" />
            </div>
          </div>
        </div>
        <button type="submit" className="w-full bg-primary-200 text-primary-900 py-4 rounded-3xl font-bold hover:bg-primary-300 shadow-xl shadow-gray-200 transition-all text-lg">Register Bus</button>
      </form>
      </div>
    </div>
  );
};

// --- SUB-COMPONENT: EXPANDABLE ROUTE CARD ---
const RouteCard = ({ route }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [stops, setStops] = useState([]);
  const [editingStop, setEditingStop] = useState(null);

  useEffect(() => {
    if (!isExpanded) return;
    const q = query(collection(db, "stops"), where("routeId", "==", route.id));
    return onSnapshot(q, (snap) => {
      const s = snap.docs.map(d => ({ id: d.id, ...d.data() }))
        .sort((a, b) => (parseInt(a.stopOrder) || 0) - (parseInt(b.stopOrder) || 0));
      setStops(s);
    });
  }, [isExpanded, route.id]);

  const handleUpdateStop = async (e) => {
    e.preventDefault();
    const { id, ...data } = editingStop;
    await updateDoc(doc(db, "stops", id), {
      ...data,
      latitude: parseFloat(data.latitude),
      longitude: parseFloat(data.longitude),
      stopOrder: parseInt(data.stopOrder) || 0
    });
    setEditingStop(null);
    alert("Stop updated successfully!");
  };

  return (
    <div className="bg-white rounded-3xl border border-gray-100 shadow-lg overflow-hidden hover:shadow-xl transition-all text-left">
      <div className="p-6 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <span className="bg-primary-50 text-primary-600 px-4 py-2 rounded-2xl font-bold border border-primary-100">{route.id}</span>
          <div>
            <p className="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Route Path:</p>
            <p className="text-sm font-bold text-gray-800 leading-tight">{route.path}</p>
          </div>
        </div>
        <button onClick={() => setIsExpanded(!isExpanded)} className="text-primary-600 text-xs font-black uppercase hover:underline">
          {isExpanded ? 'Hide Stops' : `View All (${route.stopsCount})`}
        </button>
      </div>
      {isExpanded && (
        <div className="px-6 pb-6 pt-2 border-t border-gray-50 bg-gray-50/50 space-y-3 animate-in slide-in-from-top-2 duration-300">
          {stops.length > 0 ? (
            stops.map((stop, i) => (
              <div key={stop.id} className="flex items-start gap-4 p-3 bg-white/60 rounded-2xl border border-white">
                <div className="text-xs font-black text-gray-300 mt-0.5">{stop.stopOrder || i + 1}.</div>
                <div className="flex-1">
                  <p className="text-sm font-bold text-gray-700">{stop.stopName}</p>
                  <p className="text-[10px] text-gray-400 font-medium">Lat: {stop.latitude}, Lng: {stop.longitude}</p>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => setEditingStop(stop)} className="text-primary-400 hover:text-primary-600 transition-colors"><Edit size={14} /></button>
                  <button
                    onClick={async () => {
                      if (window.confirm(`Are you sure you want to delete the stop "${stop.stopName}"?`)) {
                        await deleteDoc(doc(db, "stops", stop.id));
                      }
                    }}
                    className="p-1.5 text-red-400 hover:bg-red-50 hover:text-red-600 rounded-lg transition-all"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            ))
          ) : (
            <p className="text-center py-4 text-xs text-gray-400 italic">No stops found.</p>
          )}
        </div>
      )}

      {/* Stop Edit Modal */}
      {editingStop && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden">
            <div className="bg-primary-600 p-5 text-white flex justify-between items-center">
              <h2 className="font-bold">Edit Stop</h2>
              <button onClick={() => setEditingStop(null)}><X size={20} /></button>
            </div>
            <form onSubmit={handleUpdateStop} className="p-6 space-y-4">
              <div>
                <label className="text-[10px] font-bold text-gray-400 uppercase">Stop Name</label>
                <input value={editingStop.stopName} onChange={e => setEditingStop({ ...editingStop, stopName: e.target.value })} className="w-full mt-1 px-4 py-2 bg-gray-50 border border-gray-100 rounded-xl outline-none" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-bold text-gray-400 uppercase">Latitude</label>
                  <input type="number" step="any" value={editingStop.latitude} onChange={e => setEditingStop({ ...editingStop, latitude: e.target.value })} className="w-full mt-1 px-4 py-2 bg-gray-50 border border-gray-100 rounded-xl outline-none" />
                </div>
                <div>
                  <label className="text-[10px] font-bold text-gray-400 uppercase">Longitude</label>
                  <input type="number" step="any" value={editingStop.longitude} onChange={e => setEditingStop({ ...editingStop, longitude: e.target.value })} className="w-full mt-1 px-4 py-2 bg-gray-50 border border-gray-100 rounded-xl outline-none" />
                </div>
              </div>
              <button type="submit" className="w-full bg-primary-600 text-white py-3 rounded-xl font-bold hover:bg-primary-700 transition-all mt-2">Update Stop</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

// --- COMPONENT: TERMINALS MANAGEMENT ---
export const TerminalsManagement = ({ searchQuery = "" }) => {
  const [formData, setFormData] = useState({ stopName: '', latitude: '', longitude: '', routeId: '', stopOrder: '' });
  const [uniqueRoutes, setUniqueRoutes] = useState([]);

  useEffect(() => {
    return onSnapshot(collection(db, "stops"), (snap) => {
      const routesMap = {};
      const allStops = snap.docs.map(d => d.data());

      allStops.forEach(data => {
        if (data.routeId) {
          if (!routesMap[data.routeId]) routesMap[data.routeId] = [];
          routesMap[data.routeId].push(data);
        }
      });

      const formatted = Object.keys(routesMap).map(routeId => {
        const sorted = routesMap[routeId].sort((a, b) => (a.stopOrder || 0) - (b.stopOrder || 0));
        let path = "Empty Route";
        if (sorted.length >= 2) path = `${sorted[0].stopName} ➔ ${sorted[sorted.length - 1].stopName}`;
        else if (sorted.length === 1) path = `${sorted[0].stopName} (Single)`;

        return { id: routeId, stopsCount: sorted.length, path };
      });

      setUniqueRoutes(formatted.sort((a, b) => a.id.localeCompare(b.id)));
    });
  }, []);

  const filteredRoutes = uniqueRoutes.filter(route =>
    (route.id || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (route.path || "").toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleAddStop = async (e) => {
    e.preventDefault();
    await addDoc(collection(db, "stops"), {
      ...formData,
      latitude: parseFloat(formData.latitude),
      longitude: parseFloat(formData.longitude),
      stopOrder: parseInt(formData.stopOrder),
      timestamp: serverTimestamp()
    });
    alert("Stop Added Successfully!");
    setFormData({ stopName: '', latitude: '', longitude: '', routeId: '', stopOrder: '' });
  };

  const useGPS = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((pos) => {
        setFormData(p => ({ ...p, latitude: pos.coords.latitude.toString(), longitude: pos.coords.longitude.toString() }));
      });
    }
  };

  return (
    <div className="min-h-full flex flex-col items-center p-4 lg:p-8 animate-in fade-in duration-500">
      <div className="w-full max-w-2xl text-left">
        <div className="bg-primary-600 p-8 rounded-t-3xl text-white shadow-xl relative overflow-hidden">
          <div className="relative z-10">
            <h1 className="text-3xl font-bold">Terminal & Fermata</h1>
            <p className="text-primary-100 mt-2 font-medium">Configure transit stations and order</p>
          </div>
        </div>
        <form onSubmit={handleAddStop} className="bg-white p-8 rounded-b-3xl border border-gray-100 shadow-2xl space-y-6 -mt-4 relative z-20">
          <input required value={formData.stopName} onChange={e => setFormData({ ...formData, stopName: e.target.value })} placeholder="Stop Name (e.g. Megenagna)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
          <div className="grid grid-cols-2 gap-4">
            <input required value={formData.latitude} onChange={e => setFormData({ ...formData, latitude: e.target.value })} placeholder="Latitude" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
            <input required value={formData.longitude} onChange={e => setFormData({ ...formData, longitude: e.target.value })} placeholder="Longitude" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
          <button type="button" onClick={useGPS} className="flex items-center gap-2 text-primary-600 font-bold text-xs hover:underline uppercase tracking-widest px-1"><MapPin size={14} /> Use My Current GPS</button>
          <input required value={formData.routeId} onChange={e => setFormData({ ...formData, routeId: e.target.value })} placeholder="Route ID (e.g. MEG_44)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
          <input required value={formData.stopOrder} onChange={e => setFormData({ ...formData, stopOrder: e.target.value })} placeholder="Stop Order (1, 2, 3...)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
          <button type="submit" className="w-full bg-primary-200 text-primary-900 py-4 rounded-3xl font-bold hover:bg-primary-300 transition-all text-lg shadow-xl shadow-gray-100">Add Fermata</button>
        </form>
        <div className="mt-12 space-y-6">
          <h2 className="text-xl font-bold text-gray-800 ml-2">Existing Stations</h2>
          <div className="space-y-4 pb-12">
            {filteredRoutes.length > 0 ? (
              filteredRoutes.map(route => <RouteCard key={route.id} route={route} />)
            ) : (
              <div className="text-center py-12 bg-gray-50 rounded-3xl border border-dashed border-gray-200 text-gray-400 italic">No stations match your search.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// --- OTHERS ---
export const LiveMap = () => <div className="p-8 text-left"><h1 className="text-2xl font-bold text-gray-800">Live Map</h1><p className="text-gray-500">Real-time bus tracking portal.</p></div>;
// --- COMPONENT: SETTINGS (Profile & App Configuration) ---
export const SettingsPage = () => {
  const auth = getAuth();
  const user = auth.currentUser;
  const [name, setName] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [notifications, setNotifications] = useState(true);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      const unsub = onSnapshot(doc(db, "users", user.uid), (snap) => {
        if (snap.exists()) setName(snap.data().name || '');
      });
      return unsub;
    }
  }, [user]);

  const handleSave = async () => {
    if (!user) return;
    setLoading(true);
    try {
      // Update Name
      await updateDoc(doc(db, "users", user.uid), { name });

      // Update Password if provided
      if (newPassword) {
        if (newPassword.length < 6) {
          alert("Password must be at least 6 characters.");
        } else {
          await updatePassword(user, newPassword);
          setNewPassword('');
        }
      }

      alert("Settings saved successfully!");
    } catch (error) {
      alert("Error updating profile: " + error.message);
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-primary-400 to-primary-600 p-8 flex flex-col items-center animate-in fade-in duration-700">
      <div className="flex items-center justify-between w-full max-w-md mb-8 text-white">
        <Link to="/" className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center hover:bg-white/30 transition-all">
          <ChevronRight size={24} className="rotate-180" />
        </Link>
        <h1 className="text-3xl font-bold">Settings</h1>
        <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
          <Settings size={20} />
        </div>
      </div>

      <div className="bg-white w-full max-w-md rounded-[40px] shadow-2xl p-8 space-y-8 animate-in slide-in-from-bottom-8 duration-500">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 bg-primary-50 rounded-full flex items-center justify-center text-primary-500">
            <Users size={32} />
          </div>
          <div>
            <p className="text-xs text-gray-400 font-bold uppercase tracking-widest">Account</p>
            <p className="text-lg font-bold text-gray-800 break-all">{user?.email || 'Admin User'}</p>
          </div>
        </div>

        <div className="space-y-6">
          <div className="space-y-2 text-left">
            <div className="flex items-center gap-2 text-primary-600 font-bold text-sm">
              <Edit size={18} />
              <span>Username</span>
            </div>
            <input
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="Enter your name"
              className="w-full px-5 py-4 bg-gray-50 border border-gray-200 rounded-3xl outline-none focus:ring-2 focus:ring-primary-400 font-bold text-gray-700"
            />
          </div>

          <div className="space-y-2 text-left">
            <div className="flex items-center gap-2 text-primary-600 font-bold text-sm">
              <Settings size={18} />
              <span>New Password (optional)</span>
            </div>
            <input
              type="password"
              value={newPassword}
              onChange={e => setNewPassword(e.target.value)}
              placeholder="Enter new password"
              className="w-full px-5 py-4 bg-gray-50 border border-gray-200 rounded-3xl outline-none focus:ring-2 focus:ring-primary-400 font-bold text-gray-700"
            />
          </div>

          <div className="flex items-center justify-between py-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-indigo-50 text-indigo-500 rounded-full flex items-center justify-center">
                <Bell size={20} />
              </div>
              <span className="font-bold text-gray-700">Push Notifications</span>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" checked={notifications} onChange={() => setNotifications(!notifications)} className="sr-only peer" />
              <div className="w-14 h-8 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[4px] after:left-[4px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-6 after:w-6 after:transition-all peer-checked:bg-primary-500"></div>
            </label>
          </div>
        </div>

        <button
          onClick={handleSave}
          disabled={loading}
          className="w-full bg-primary-500 text-white py-5 rounded-[25px] font-bold text-xl hover:bg-primary-600 transition-all shadow-xl shadow-primary-100 flex items-center justify-center gap-2 disabled:opacity-50"
        >
          {loading ? 'Saving...' : 'Save Changes'}
        </button>
      </div>

      <Link to="/" className="mt-12 flex items-center gap-3 text-white font-bold hover:scale-105 transition-transform">
        <MapIcon size={24} />
        <span className="tracking-widest uppercase text-sm">Back to Map</span>
      </Link>
    </div>
  );
};
// --- COMPONENT: ROUTE MANAGEMENT (Real-time Network Sync) ---
export const RouteManagement = ({ searchQuery = "" }) => {
  const [formData, setFormData] = useState({ routeId: '', routeName: '', busNumber: '' });
  const [routes, setRoutes] = useState([]);
  const [editingRoute, setEditingRoute] = useState(null);

  useEffect(() => {
    return onSnapshot(collection(db, "routes"), (snap) => {
      setRoutes(snap.docs.map(d => ({ id: d.id, ...d.data() })));
    });
  }, []);

  const filteredRoutes = routes.filter(r =>
    (r.routeId || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (r.routeName || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (r.busNumber || "").toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    await setDoc(doc(db, "routes", formData.routeId), formData);
    alert("Route Initialized Successfully!");
    setFormData({ routeId: '', routeName: '', busNumber: '' });
  };

  const handleUpdateRoute = async (e) => {
    e.preventDefault();
    const { id, ...data } = editingRoute;
    await updateDoc(doc(db, "routes", id), data);
    setEditingRoute(null);
  };

  return (
    <div className="p-8 text-left max-w-2xl mx-auto animate-in fade-in duration-500">
      <div className="bg-primary-600 p-8 rounded-t-3xl text-white shadow-xl relative overflow-hidden">
        <div className="relative z-10">
          <h1 className="text-3xl font-bold">Route Management</h1>
          <p className="text-primary-100 mt-2 font-medium">Define and optimize city pathways</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-b-3xl border border-gray-100 shadow-2xl space-y-6 -mt-4 relative z-20">
        <h3 className="text-primary-600 font-bold text-sm ml-1">Create New Route</h3>
        <input required value={formData.routeId} onChange={e => setFormData({ ...formData, routeId: e.target.value })} placeholder="Route ID (e.g. MEG_44)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
        <input required value={formData.routeName} onChange={e => setFormData({ ...formData, routeName: e.target.value })} placeholder="Route Name (e.g. Megenagna - Abado)" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
        <input required value={formData.busNumber} onChange={e => setFormData({ ...formData, busNumber: e.target.value })} placeholder="Bus Number" className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />

        <button type="submit" className="w-full bg-primary-200 text-primary-900 py-4 rounded-3xl font-bold hover:bg-primary-300 transition-all text-lg shadow-xl shadow-gray-100">
          Initialize Route
        </button>
      </form>

      <div className="mt-12 space-y-6">
        <h2 className="text-xl font-bold text-gray-800 ml-2">Active Network</h2>
        <div className="space-y-6">
          {filteredRoutes.length > 0 ? (
            filteredRoutes.map(r => (
              <div key={r.id} className="group flex items-center justify-between p-4 bg-white rounded-2xl border border-gray-50 hover:border-primary-100 transition-all shadow-sm">
                <div className="flex flex-col">
                  <h4 className="text-lg font-bold text-indigo-900 group-hover:text-primary-600 transition-colors">{r.routeName}</h4>
                  <p className="text-xs text-gray-400 font-bold uppercase tracking-widest">
                    ID: {r.routeId} <span className="mx-2 text-gray-200">|</span> Bus: {r.busNumber}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => setEditingRoute(r)} className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors">
                    <Edit size={18} />
                  </button>
                  <button onClick={async () => { if (window.confirm("Delete this route?")) await deleteDoc(doc(db, "routes", r.id)) }} className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors">
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="text-center py-12 text-gray-400 italic">No routes match your search.</div>
          )}
        </div>
      </div>

      {/* Route Edit Modal */}
      {editingRoute && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-md rounded-3xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-300">
            <div className="bg-primary-600 p-6 text-white flex justify-between items-center">
              <h3 className="text-xl font-bold">Edit Route</h3>
              <button onClick={() => setEditingRoute(null)} className="p-2 hover:bg-white/20 rounded-full"><X size={20} /></button>
            </div>
            <form onSubmit={handleUpdateRoute} className="p-8 space-y-6">
              <div>
                <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Route Name</label>
                <input value={editingRoute.routeName} onChange={e => setEditingRoute({ ...editingRoute, routeName: e.target.value })} className="w-full mt-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
              </div>
              <div>
                <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Assigned Bus</label>
                <input value={editingRoute.busNumber} onChange={e => setEditingRoute({ ...editingRoute, busNumber: e.target.value })} className="w-full mt-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
              </div>
              <button type="submit" className="w-full bg-primary-600 text-white py-4 rounded-2xl font-bold hover:bg-primary-700 transition-all shadow-lg shadow-primary-100">Save Changes</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export const ComplaintsManagement = ({ searchQuery = "" }) => {
  const [complaints, setComplaints] = useState([]);
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyText, setReplyText] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    return onSnapshot(query(collection(db, "complaints"), orderBy("timestamp", "desc")), (s) =>
      setComplaints(s.docs.map(d => ({ id: d.id, ...d.data() })))
    );
  }, []);

  const filteredComplaints = complaints.filter(c =>
    (c.userEmail || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (c.subject || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (c.message || "").toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleReply = async () => {
    if (!replyText.trim()) return;
    setLoading(true);
    try {
      await updateDoc(doc(db, "complaints", replyingTo.id), {
        adminReply: replyText,
        status: "resolved",
        repliedAt: serverTimestamp()
      });
      setReplyingTo(null);
      setReplyText('');
      alert("Reply sent successfully!");
    } catch (e) {
      alert("Error sending reply: " + e.message);
    }
    setLoading(false);
  };

  const handleDelete = async (id) => {
    if (window.confirm("Are you sure you want to delete this complaint?")) {
      try {
        await deleteDoc(doc(db, "complaints", id));
      } catch (e) {
        alert("Error deleting: " + e.message);
      }
    }
  };

  return (
    <div className="p-8 text-left animate-in fade-in duration-500">
      <h1 className="text-2xl font-bold text-gray-800 mb-8">User Complaints</h1>
      <div className="space-y-4">
        {filteredComplaints.length > 0 ? filteredComplaints.map(c => (
          <div key={c.id} className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex gap-6 hover:shadow-md transition-all">
            <div className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 ${c.status === 'pending' ? 'bg-orange-100 text-orange-600' : 'bg-green-100 text-green-600'}`}>
              <MessageSquare size={24} />
            </div>
            <div className="flex-1">
              <div className="flex justify-between mb-2">
                <div>
                  <p className="font-bold text-gray-800">{c.subject || "No Subject"}</p>
                  <p className="text-xs text-gray-400">{c.userEmail}</p>
                </div>
                <span className={`px-2 py-1 rounded text-[10px] font-bold uppercase ${c.status === 'pending' ? 'bg-orange-100 text-orange-600' : 'bg-green-100 text-green-600'}`}>
                  {c.status}
                </span>
              </div>
              <p className="text-gray-600 text-sm mb-4">"{c.message}"</p>

              {c.adminReply && (
                <div className="mb-4 p-3 bg-blue-50 rounded-xl border border-blue-100">
                  <p className="text-[10px] font-bold text-blue-600 uppercase mb-1">Your Reply:</p>
                  <p className="text-sm text-blue-800 italic">"{c.adminReply}"</p>
                </div>
              )}

              <div className="flex gap-2">
                <button
                  onClick={() => setReplyingTo(c)}
                  className="px-4 py-2 text-xs font-bold text-primary-600 hover:bg-primary-50 rounded-lg flex items-center gap-2"
                >
                  <Send size={14} /> {c.adminReply ? 'Update Reply' : 'Reply'}
                </button>
                <button
                  onClick={() => handleDelete(c.id)}
                  className="px-4 py-2 text-xs font-bold text-red-500 hover:bg-red-50 rounded-lg flex items-center gap-2"
                >
                  <Trash2 size={14} /> Delete
                </button>
              </div>
            </div>
          </div>
        )) : (
          <div className="text-center py-20 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200 text-gray-400">
            No complaints found.
          </div>
        )}
      </div>

      {/* --- REPLY MODAL --- */}
      {replyingTo && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-lg rounded-3xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-300">
            <div className="bg-primary-600 p-6 text-white flex justify-between items-center">
              <div>
                <h3 className="text-xl font-bold">Reply to Complaint</h3>
                <p className="text-primary-100 text-xs mt-1">To: {replyingTo.userEmail}</p>
              </div>
              <button onClick={() => setReplyingTo(null)} className="p-2 hover:bg-white/20 rounded-full transition-colors">
                <X size={20} />
              </button>
            </div>
            <div className="p-6 space-y-6">
              <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                <p className="text-xs font-bold text-gray-400 uppercase mb-2">User Message:</p>
                <p className="text-gray-700 italic">"{replyingTo.message}"</p>
              </div>
              <textarea
                rows="5"
                value={replyText}
                onChange={e => setReplyText(e.target.value)}
                placeholder="Type your response here..."
                className="w-full p-4 bg-gray-50 border border-gray-200 rounded-2xl outline-none focus:ring-2 focus:ring-primary-500 text-gray-800 transition-all"
              />
              <div className="flex gap-4">
                <button
                  onClick={() => setReplyingTo(null)}
                  className="flex-1 py-3 font-bold text-gray-500 bg-gray-100 rounded-2xl hover:bg-gray-200 transition-all"
                >
                  Cancel
                </button>
                <button
                  onClick={handleReply}
                  disabled={loading}
                  className="flex-1 py-3 font-bold text-white bg-primary-600 rounded-2xl hover:bg-primary-700 transition-all shadow-lg shadow-primary-100 flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {loading ? 'Sending...' : <><Send size={18} /> Send Reply</>}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// --- COMPONENT: NEWS MANAGEMENT (Broadcast Sync with Expand/Collapse) ---
export const NewsManagement = ({ searchQuery = "" }) => {
  const [formData, setFormData] = useState({ title: '', content: '' });
  const [news, setNews] = useState([]);
  const [showAll, setShowAll] = useState(false);

  useEffect(() => {
    const q = query(collection(db, "news"), orderBy("timestamp", "desc"));
    return onSnapshot(q, (snap) => {
      setNews(snap.docs.map(d => ({ id: d.id, ...d.data() })));
    });
  }, []);

  const filteredNews = news.filter(n =>
    (n.title || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
    (n.content || "").toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newsId = `NEWS_${Date.now()}`;
    await setDoc(doc(db, "news", newsId), { ...formData, newsId, timestamp: serverTimestamp() });
    alert("News Posted Successfully!");
    setFormData({ title: '', content: '' });
  };

  const formatTime = (ts) => {
    if (!ts) return "Just now";
    const date = ts.toDate();
    return date.getHours().toString().padStart(2, '0') + ":" + date.getMinutes().toString().padStart(2, '0');
  };

  const displayedNews = showAll ? filteredNews : filteredNews.slice(0, 3);

  return (
    <div className="p-8 text-left max-w-2xl mx-auto animate-in fade-in duration-500">
      <div className="bg-primary-600 p-8 rounded-t-3xl text-white shadow-xl relative overflow-hidden">
        <div className="relative z-10">
          <h1 className="text-3xl font-bold">Manage News</h1>
          <p className="text-primary-100 mt-2 font-medium">Broadcast updates to all passengers</p>
        </div>
      </div>

      <div className="bg-white p-8 rounded-b-3xl border border-gray-100 shadow-2xl -mt-4 relative z-20">
        <div className="flex items-center gap-4 mb-8">
          <div className="w-12 h-12 bg-primary-50 rounded-full flex items-center justify-center text-primary-500"><Edit size={24} /></div>
          <div>
            <h2 className="text-xl font-bold text-indigo-900">Post News/Updates</h2>
            <p className="text-xs text-gray-400">Share the latest news and updates</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="relative">
            <Edit className="absolute left-4 top-4 text-gray-300" size={20} />
            <input required value={formData.title} onChange={e => setFormData({ ...formData, title: e.target.value })} placeholder="News Title" className="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 font-bold" />
          </div>
          <div className="relative">
            <MessageSquare className="absolute left-4 top-4 text-gray-300" size={20} />
            <textarea required rows="4" value={formData.content} onChange={e => setFormData({ ...formData, content: e.target.value })} placeholder="News Content/Description" className="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500" />
          </div>

          <button type="submit" className="w-full bg-primary-500 text-white py-4 rounded-3xl font-bold hover:bg-primary-600 transition-all text-lg shadow-xl shadow-primary-100 flex items-center justify-center gap-3">
            <Send size={20} /> Post News Update
          </button>
        </form>
      </div>

      <div className="mt-12 space-y-4">
        <div className="flex justify-between items-center px-2">
          <h2 className="text-xl font-bold text-gray-800">Recent Posts</h2>
          <div className="flex gap-4">
            <button
              onClick={async () => {
                if (window.confirm("Are you sure? This will permanently delete ALL recent posts and fleet updates.")) {
                  try {
                    const snap = await getDocs(collection(db, "news"));
                    let count = 0;
                    for (const d of snap.docs) {
                      await deleteDoc(doc(db, "news", d.id));
                      count++;
                    }
                    alert(`Success! Cleared ${count} items.`);
                  } catch (err) {
                    alert("Error clearing alerts: " + err.message);
                  }
                }
              }}
              className="text-red-600 text-xs font-black uppercase tracking-tighter hover:bg-red-50 px-3 py-1.5 rounded-lg border border-red-100 transition-all"
            >
              Clear All Alerts
            </button>
            {news.length > 3 && (
              <button onClick={() => setShowAll(!showAll)} className="text-primary-600 text-sm font-bold hover:underline">
                {showAll ? "Show Less" : `View All (${news.length})`}
              </button>
            )}
          </div>
        </div>
        <div className="space-y-4">
          {displayedNews.length > 0 ? displayedNews.map(n => (
            <div key={n.id} className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex gap-4 animate-in slide-in-from-bottom-2 duration-300 group">
              <div className="w-10 h-10 bg-indigo-50 rounded-lg flex items-center justify-center text-indigo-500 shrink-0"><Bell size={18} /></div>
              <div className="flex-1">
                <div className="flex justify-between items-start mb-1">
                  <h3 className="font-bold text-gray-800">{n.title}</h3>
                  <div className="flex items-center gap-3">
                    <span className="text-[10px] text-gray-400 font-medium">{formatTime(n.timestamp)}</span>
                    <button onClick={async () => await deleteDoc(doc(db, "news", n.id))} className="text-gray-300 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all"><Trash2 size={14} /></button>
                  </div>
                </div>
                <p className="text-xs text-gray-500 leading-relaxed">{n.content}</p>
              </div>
            </div>
          )) : (
            <div className="text-center py-12 text-gray-400 italic bg-white rounded-2xl border border-dashed border-gray-100">No news found matching your search.</div>
          )}
        </div>
      </div>
    </div>
  );
};
