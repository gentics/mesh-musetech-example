import React from 'react';
import logoFile from '../img/logo.svg';
import { Container, Col, Row } from 'react-bootstrap';

const logo = {
  src: logoFile,
  alt: 'Blackspring History Museum',
};

export default function Title() {
  return (
    <Container>
      <Row>
        <Col>
          <a href="/"><img className="title-logo img-responsive" src={logo.src} alt={logo.alt} /></a>
        </Col>
      </Row>
    </Container>
  )
}
