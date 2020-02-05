import { Link } from 'react-router-dom';
import { getTours } from '../api';
import Navigation from '../components/Navigation';
import Header from '../components/Header';
import Footer from '../components/Footer';
import React, { useState, useEffect, useContext } from 'react';
import useWebsocketBridge from '../eventbus';
import { Col, Row, Container } from 'react-bootstrap';
import LanguageContext from '../languageContext';
import config from '../config.json';

const i18n = {
    en: {
        heading: "Discover new worlds",
        tours: "Tours"
    },
    de: {
        heading: "Entdecke neue Welten",
        tours: "Führungen"
    }
}

export default function TourList({ match, location }) {

    let lang = useContext(LanguageContext);
    let trans = i18n[lang];

    return (
        <>
            <Navigation />
            <Header lead={trans.tours} heading={trans.heading} className="tourhead" />

            <section className="page-section bg-light">
                <SearchForm location={location} />
                <Container>
                    <Row>
                        <Col md={12} className="text-center">
                            <p className="text-muted">Adipisicing in aliqua enim mollit laboris consectetur eiusmod eu ut est labore laboris. Occaecat cupidatat officia commodo do ea non duis voluptate voluptate sunt magna. Id tempor quis mollit nostrud tempor velit exercitation. Minim dolore laboris eiusmod ut magna ex non exercitation duis proident in. Laborum voluptate exercitation excepteur id officia nostrud sint cupidatat ex sunt. Cupidatat exercitation ipsum consectetur laborum ut officia adipisicing aute mollit adipisicing eu dolor. Amet non et sint aliquip cillum cillum. Duis aute ex excepteur dolore Lorem labore excepteur id do in in veniam labore et. Veniam non minim voluptate ad consectetur reprehenderit. Exercitation id eu occaecat commodo labore aliquip do cupidatat deserunt consequat quis proident.</p>
                        </Col>
                    </Row>
                </Container>
            </section>

            <Footer />
        </>
    );
}

function SearchForm({ location }) {

    // Create state for the component
    const [nodeResponse, setNodeResponse] = useState();

    let lang = useContext(LanguageContext);
    let trans = i18n[lang];

    // Register event callback to update the state when content gets changed in Gentics Mesh    
    useWebsocketBridge(() => {
        getTours(lang, 1).then(setNodeResponse);
    });

    useEffect(() => {
        getTours(lang, 1).then(setNodeResponse);
    }, [lang]);

    let results = [];

    if (nodeResponse && nodeResponse.children && nodeResponse.children.elements) {
        results = nodeResponse.children.elements;
    }

    if (nodeResponse && nodeResponse.nodes && nodeResponse.nodes.elements) {
        results = nodeResponse.nodes.elements;
    }

    const emptyPlaceholder = (
        <Col md={12} className="text-center text-muted">
            <Container>{trans.not_found}</Container>
        </Col>
    );

    const resultList = results.length ? (results.filter(tour => {
        return tour.fields.image != null;
    }).map(tour => (
        <Tour tour={tour} key={tour.uuid} />
    ))) : emptyPlaceholder


    return (
        <div className="search-area">
            <Row>
                {resultList}
            </Row>
        </div>
    )

}

function Tour({ tour }) {
    let lang = useContext(LanguageContext);
    let attribution = tour.fields.image.fields.attribution;
    let image = tour.fields.image;
    let color = image.fields.binary.dominantColor;
    return (
        <Col md={3} className="tour-item">
            <Link to={`/${lang}/tours/${tour.fields.public_number}`} className="tour-link">
                <picture style={{ background: color }}>
                    <source media="(min-width: 320px)" srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=300&w=500&mode=smart&crop=fp`}></source>
                    <source media="(min-width: 786px)" srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=400&w=500&mode=smart&crop=fp`}></source>
                    <source media="(min-width: 1280px)" srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=500&w=500&mode=smart&crop=fp`}></source>
                    <img alt={tour.fields.name} srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=300&w=300&mode=smart&crop=fp`} className="img-responsive img-fluid" />
                </picture>
                <div className="image-attribution">
                    <p>{attribution}</p>
                </div>
            </Link>
            <div className="tour-caption">
                <p>{tour.fields.name}</p>
            </div>
        </Col>
    )
}
