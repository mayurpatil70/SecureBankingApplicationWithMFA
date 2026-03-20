import React from "react";
import { Link, useNavigate } from "react-router-dom";
import "./Navbar.css";

function Navbar({ user, onLogout }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <h2>🏦 Secure Banking</h2>
        </div>

        <div className="navbar-menu">
          <Link to="/dashboard" className="nav-link">
            Dashboard
          </Link>
          <Link to="/transfer" className="nav-link">
            Transfer
          </Link>
          <Link to="/transactions" className="nav-link">
            History
          </Link>
          <Link to="/statements" className="nav-link">
            Statements
          </Link>
          <Link to="/profile" className="nav-link">
            Profile
          </Link>
        </div>

        <div className="navbar-user">
          <div className="user-info">
            <span className="user-name">
              {user?.firstName} {user?.lastName}
            </span>
            <span className="user-email">{user?.email}</span>
          </div>
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
