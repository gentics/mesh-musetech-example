import React from 'react';
import Content from './Content';
import { BrowserRouter as Router } from 'react-router-dom';
import Keycloak from 'keycloak-js';
import { KeycloakProvider } from 'react-keycloak';

const keycloak = new Keycloak({
  url: 'http://localhost:8082/auth',
  realm: 'master-test',
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