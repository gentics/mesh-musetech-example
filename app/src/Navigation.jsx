import React from 'react';
import { Link } from 'react-router-dom';
import { getNavigation, usePromise } from './api';

export default function Navigation() {
  const navResponse = usePromise(() => getNavigation(), []);

  return (
    <nav className="navbar navbar-default row">
      <div className="col-md-12">
        <ul className="nav navbar-nav padding-left-50">
          {navResponse && navResponse.project.rootNode.children.elements.map(category => (
            <NavElement key={category.uuid} category={category} />
          ))}
          <li>
            <Link to={`/videos`}>Videos</Link>
          </li>
          <li>
            <Link to={`/screens`}>Screens</Link>
          </li>
          <li>
            <Link to={`/exhibitions`}>Exhibitions</Link>
          </li>
        </ul>
      </div>
    </nav>
  )
}

function NavElement({ category }) {
  return (
    <li>
      <Link to={`/category/${category.uuid}`}>
        {category.fields.name}
      </Link>
    </li>
  )
}