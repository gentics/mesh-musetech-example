import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { getExhibit } from '../api';
import Navigation from '../components/Navigation';
import Footer from '../components/Footer';
import useWebsocketBridge from '../eventbus';
import { Col, Row, Container } from 'react-bootstrap';
import LanguageContext from '../languageContext';
import config from '../config.json';

const trans = {
    de: {
        building: "Gebäude",
        level: "Ebene",
        section: "Bereich"
    },
    en: {
        building: "Building",
        level: "Level",
        section: "Section"
    }
}
export default function ExhibitView({ match }) {
    // Create state for the component
    const [exhibit, setExhibit] = useState();
    const id = match.params.id;
    let lang = useContext(LanguageContext);

    // Register event callback to update the state when content gets changed in Gentics Mesh
    useWebsocketBridge(() => {
        getExhibit(id, lang).then(setExhibit);
    });

    // Use effect hook to set the content when the path changes
    useEffect(() => {
        getExhibit(id, lang).then(setExhibit);
    }, [id, lang]);

    if (!exhibit) {
        return null;
    }


    let location = exhibit.fields.location;
    let i18n = trans[lang];

    return (
        <>
            <Navigation />
            <section className="page-section without-header">
                <Container>
                    <div className="content exhibit-detail-caption bg-light" >
                        <Row>
                            <Col lg={12} className="text-center">
                                <div className="exhibit-title">
                                    <h2 className="section-heading"><Link to={`/${lang}/exhibits`}>&lt;&lt;</Link>&nbsp;{exhibit.fields.name}</h2>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg={12}>
                                <picture>
                                    <source media="(min-height: 320px)" srcSet={`${config.meshUrl}/musetech/webroot${exhibit.fields.title_image.path}?w=500&mode=smart`}></source>
                                    <source media="(min-height: 786px)" srcSet={`${config.meshUrl}/musetech/webroot${exhibit.fields.title_image.path}?w=800&mode=smart`}></source>
                                    <source media="(min-height: 1280px)" srcSet={`${config.meshUrl}/musetech/webroot${exhibit.fields.title_image.path}?w=1200&mode=smart`}></source>
                                    <img alt={exhibit.fields.name} srcSet={`${config.meshUrl}/musetech/webroot${exhibit.fields.title_image.path}?w=600&mode=smart`} className="img-responsive img-fluid" />
                                </picture>
                                <div className="image-attribution">
                                    <p>{exhibit.fields.title_image.fields.attribution}</p>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg={12} className="exhibit-location text-muted">
                                <div>
                                    <b>{i18n.building}:</b> {location.fields.building} &nbsp;&nbsp;                            
                                    <b>{i18n.level}:</b> {location.fields.level} &nbsp;&nbsp;                                
                                    <b>{i18n.section}:</b> {location.fields.section}
                                </div>
                            </Col>
                        </Row>
                        <Row> 
                            <Col lg={{ span: 6, offset: 3 }} className="text-left text-muted">
                                <b>Audio Guide</b>
                            </Col>
                        </Row>
                        <Row> 
                            <Col lg={{ span: 6, offset: 3 }} className="text-left">
                                <Player data={exhibit.fields.audio} />
                            </Col>
                        </Row>
                        <Row>
                            <Col lg={{ span: 8, offset: 2 }} className="text-center">
                                <div className="exhibit-detail-caption">
                                    <p className="text-muted">{exhibit.fields.description}</p>
                                </div>
                            </Col>
                        </Row>
                    </div>
                </Container>
            </section>
            <Footer />
        </>
    );
}


function Player({ data }) {
    if (data == null || data.path === null) {
        return "";
    }

    return (
        <>
            <audio controls key={data.path}>
                <source src={`${config.meshUrl}/musetech/webroot${data.path}`} type="audio/mp3" />
                Your browser does not support the audio element.
            </audio>
        </>
    );
}
