import React from 'react';
import Navigation from '../components/Navigation';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { Row, Container, Col } from 'react-bootstrap';
import config from '../config.json';

export default function HistoryPage({ content }) {
    return (
        <>
            <Navigation languages={content.languages} />
            <Header lead={content.fields.headline} heading={content.fields.title} className="historyhead" />
            <section className="page-section" id="history">
                <Container>
                    <Row>
                        <Col lg={12} className="text-center">
                            <h2 className="section-heading text-uppercase">{content.fields.intro}</h2>
                            <h3 className="section-subheading text-muted">Lorem ipsum dolor sit amet consectetur.</h3>
                        </Col>
                    </Row>
                    <Row>
                        <Col lg={12}>
                            <ul className="timeline">
                                {content.fields.timeline.map((section, index) => <TimeLineEntry index={index} key={content.version + section.uuid} content={section} />)}
                                <li className="timeline-inverted">
                                    <div className="timeline-image">
                                        <h4>New
                                      <br />Adventures
                                        <br />
                                            Await!</h4>
                                    </div>
                                </li>
                            </ul>
                        </Col>
                    </Row>
                </Container>
            </section>
            <Footer />
        </>)
}

function TimeLineEntry({ content, index }) {
    let imagePath = "";
    if (content.fields.image) {
        imagePath = config.meshUrl + "/musetech/webroot/" + content.fields.image.path + "?w=200&h=200";
    }
    return (
        <li className={index % 2 !== 0 ? "timeline-inverted" : undefined}>
            <div className="timeline-image">
                <img className="rounded-circle img-fluid" src={imagePath} alt="" />
            </div>
            <div className="timeline-panel">
                <div className="timeline-heading">
                    <h4>{content.fields.time}</h4>
                    <h4 className="subheading">{content.fields.subheading}</h4>
                </div>
                <div className="timeline-body">
                    <p className="text-muted">{content.fields.text}</p>
                </div>
            </div>
        </li>
    )
}