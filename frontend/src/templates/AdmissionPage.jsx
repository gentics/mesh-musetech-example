import React from 'react';
import Navigation from '../components/Navigation';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { Col, Row, Container } from 'react-bootstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChild, faCircle, faUsers, faWheelchair, faUser } from '@fortawesome/free-solid-svg-icons'


const icons = {
    child: faChild,
    users: faUsers,
    user: faUser,
    wheelchair: faWheelchair
}

export default function AdmissionPage({ content }) {
    return (
        <>
            <Navigation languages={content.languages} />
            <Header lead={content.fields.headline} heading={content.fields.title} className="pricehead" />

            <section className="page-section bg-light" id="prices">
                <Container>
                    <Row >
                        <div className="col-lg-12 text-center">
                            <h2 className="section-heading text-uppercase">{content.fields.title}</h2>
                            <h3 className="section-subheading text-muted">{content.fields.intro}</h3>
                        </div>
                    </Row>
                </Container>
            </section>

            <section className="page-section bg-light" id="services">
                <Container>
                    <Row className="text-center">
                        {content.fields.types.map((info, index) => <AdmissionInfo index={index} key={content.version + info.uuid} content={info} />)}
                    </Row>
                </Container>
            </section>
            <Footer />
        </>
    );

}

function AdmissionInfo({ content }) {

    let icon = icons[content.fields.icon];
    return (
        <Col md={4}>
            <span className="fa-stack fa-4x">
                <FontAwesomeIcon icon={faCircle} className="fas fa-stack-2x text-primary" />
                <FontAwesomeIcon icon={icon} className="fas fa-stack-1x fa-inverse" />
            </span>
            <h4 className="service-heading">{content.fields.title}</h4>
            <h3 className="service-heading">{content.fields.price}</h3>
            <p className="text-muted">{content.fields.description}</p>
        </Col>
    )
}