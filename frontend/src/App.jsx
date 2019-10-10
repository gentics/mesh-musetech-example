import React from 'react';
import Content from './Content';
import { BrowserRouter as Router } from 'react-router-dom';

export default class App extends React.Component {
  render() {
    return (
      <Router>
        <div className="container-fluid no-padding">
          <Content />
        </div>
      </Router>
    )
  }
}