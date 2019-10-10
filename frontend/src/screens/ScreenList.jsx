import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getScreens } from '../api';
import useWebsocketBridge from '../eventbus';
import Navigation from '../components/Navigation';
import Footer from '../components/Footer';
import { Container, Col, Row } from 'react-bootstrap';

export default function ScreenList() {

  const [screensResponse, setScreensResponse] = useState();
  useWebsocketBridge(async () => { setScreensResponse(await getScreens()) });
  useEffect(() => {
    getScreens().then(setScreensResponse);
  }, []);

  if (!screensResponse) {
    return null;
  }

  const folder = screensResponse;
  return (
    <>
      <Navigation />

      <section className="bg-black page-section without-header">
        <Container className="bg-light">
          <div className="content">
            <Row>
              <Col lg={12} className="text-center">
                <h2 className="section-heading text-uppercase">Screens</h2>
                <h3 className="section-subheading text-muted">This page contains an overview of all configured screens for digital signage.</h3>
              </Col>
            </Row>
            <Row>
              <Col lg={12} className="text-center">
                <Row>
                  {folder.children.elements.map(screen => (
                    <Screen screen={screen} key={screen.uuid} />
                  ))}
                </Row>
              </Col>
            </Row>
          </div>
        </Container>
      </section>
      <Footer/>
    </>
  );
}
function Screen({ screen }) {
  return (
    <Col xs={12} sm={6} md={4} className="screen-item border">
      <Row>
        <Col lg={12} className="text-center">
          <h3>
            <Link to={`/en/screens/${screen.fields.id}`}>{screen.fields.name} </Link>
          </h3>
        </Col>
      </Row>
      <p className="description">{screen.fields.description}</p>
      <Container className="text-left">
        <Row>
          <Col xs={12}>
            <span className="label">Slides: {screen.fields.contents.length}</span>
          </Col>
          <Col xs={12}>
          <span className="label">Location: {screen.fields.location}</span>
          </Col>
          <Col xs={12}>
          <span className="label">Status: {screenStatus()}</span>
          </Col>

        </Row>
      </Container>

    </Col>

  )
}

function screenStatus() {
  return "ONLINE";
}