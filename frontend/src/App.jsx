import React from 'react';
import Content from './Content';
import { BrowserRouter as Router } from 'react-router-dom';
import Keycloak from 'keycloak-js';
import { KeycloakProvider } from 'react-keycloak';

const keycloak = new Keycloak({
  url: 'https://sso.musetech.getmesh.io/auth',
  realm: 'master',
  clientId: 'react'
});


export default class App extends React.Component {
  render() {
    return (
      <KeycloakProvider keycloak={keycloak}>
      <Router>
        <div className="container-fluid no-padding">
          <Content />
        </div>
      </Router>
      </KeycloakProvider>
    )
  }
}