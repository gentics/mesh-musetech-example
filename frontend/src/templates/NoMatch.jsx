import React from 'react';
import Navigation from '../components/Navigation';
import Footer from '../components/Footer';
import { Container, Col, Row } from 'react-bootstrap';

export default function NoMatch({ match }) {
    return (
        <>
            <Navigation />
            <section className="page-section"/>
            <section className="page-section">
                <Container>
                    <Row>
                        <Col lg={12} className="text-center">
                            <h2 className="section-heading text-uppercase">Page Not Found</h2>
                            <h3 className="section-subheading text-muted">The page you were looking for could not be found</h3>
                        </Col>
                    </Row>
                </Container>
            </section>
            <Footer />
        </>
    );

}