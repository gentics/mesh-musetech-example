import React from 'react';
import Navigation from './Navigation';
import Title from './Title';

export default function WelcomeScreen() {
  return (
    <>
      <Title />
      <Navigation />
      <div className="jumbotron">
        <div className="row">
          <div className="col-md-6 col-md-offset-3">
            <video
              autoPlay
              muted
              width="100%">
              <source src={`/api/v1/demo/webroot/videos/intro.webm`}>
              </source>
            </video>
            <br />
          </div>
        </div>

      </div>
    </>
  );
}