import React from 'react';
import Navigation from '../components/Navigation';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { Container, Col, Row } from 'react-bootstrap';
import config from '../config.json';

export default function ContentPage({ content }) {
    return (
        <>
            <Navigation languages={content.languages} />
            <Header lead={content.fields.headline} heading={content.fields.title} className="abouthead" />

            <section className="page-section bg-dark">
                <Container>
                    <Row >
                        <Col md={12} className="text-center">
                            <div>
                                <video
                                    width="70%"
                                    controls={true}
                                    loop={true}>
                                    <source src={`${config.meshUrl}/musetech/webroot/video/intro.webm`} />
                                </video>
                            </div>
                        </Col>
                    </Row>
                </Container>
            </section>

            <section className="page-section bg-light">
                <Container>
                    <Row className="align-items-left">
                        <Col lg={12}>
                            <h2 className="section-heading text-uppercase">Attribution</h2>
                            <ul>
                                <li>Title Image: Photo by <a href="https://unsplash.com/@dariuszsankowski?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Dariusz Sankowski</a> on <a href="https://unsplash.com/">Unsplash</a></li>
                                <li>Pricing Image: Photo by <a href="https://unsplash.com/@agebarros?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Agê Barros</a> on <a href="https://unsplash.com/">Unsplash</a></li>
                                <li>Exhibition Image: Photo by <a href="https://unsplash.com/@anthonydelanoix?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Anthony DELANOIX</a> on <a href="https://unsplash.com/">Unsplash</a></li>
                                <li>History Image: Photo by <a href="https://unsplash.com/@giamboscaro?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Giammarco Boscaro</a> on <a href="https://unsplash.com/">Unsplash</a></li>
                                <li>History 1: Photo by <a href="https://unsplash.com/@cgreiter?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Chad Greiter</a> on <a href="https://unsplash.com/">Unsplash</a></li>
                                <li>History 2: Photo by <a href="https://unsplash.com/@cgreiter?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Chad Greiter</a> on <a href="https://unsplash.com/">Unsplash</a></li>
                                <li>History 3: Photo by <a href="https://unsplash.com/@omarsotillo?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Omar Sotillo Franco</a> on <a href="https://unsplash.com/">Unsplash</a></li>
                            </ul>
                        </Col>
                    </Row>
                </Container>
            </section>
            <Footer />
        </>
    );
}