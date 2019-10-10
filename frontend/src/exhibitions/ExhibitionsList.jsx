import { Link } from 'react-router-dom';
import { getExhibitions, filterExhibitions } from '../api';
import Navigation from '../components/Navigation';
import Header from '../components/Header';
import Footer from '../components/Footer';
import React, { useState, useEffect, useContext } from 'react';
import useWebsocketBridge from '../eventbus';
import { Col, Row, Container } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSearchPlus } from '@fortawesome/free-solid-svg-icons'
import LanguageContext from '../languageContext';
import config from '../config.json';

const i18n = {
    en: {
        not_found: "No results could be found",
        heading: "Discover new worlds",
        exhibits: "Exhibits"
    },
    de: {
        not_found: "Es wurde leider nichts gefunden",
        heading: "Entdecke neue Welten",
        exhibits: "Austellungen"
    }
}

export default function ExhibitionsList({ match, location }) {

    let lang = useContext(LanguageContext);
    let trans = i18n[lang];

    return (
        <>
            <Navigation />
            <Header lead={trans.exhibits} heading={trans.heading} className="exhibithead" />

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

            <section className="page-section bg-light">
                <Container>
                    <Row>
                        <Col md={4} className="text-center">
                            <p className="text-muted">Minim fugiat duis dolore duis. Eiusmod sunt qui esse est est incididunt nulla consectetur ad deserunt commodo ipsum ex. Fugiat consequat ea aliqua duis mollit esse qui do. Nisi do veniam amet veniam quis. Deserunt quis amet consequat officia sint qui dolore voluptate. Laborum ad officia ullamco occaecat do minim.</p>
                        </Col>
                        <Col md={4} className="text-center">
                            <p className="text-muted">Minim fugiat duis dolore duis. Eiusmod sunt qui esse est est incididunt nulla consectetur ad deserunt commodo ipsum ex. Fugiat consequat ea aliqua duis mollit esse qui do. Nisi do veniam amet veniam quis. Deserunt quis amet consequat officia sint qui dolore voluptate. Laborum ad officia ullamco occaecat do minim.</p>
                        </Col>
                        <Col md={4} className="text-center">
                            <p className="text-muted">Minim fugiat duis dolore duis. Eiusmod sunt qui esse est est incididunt nulla consectetur ad deserunt commodo ipsum ex. Fugiat consequat ea aliqua duis mollit esse qui do. Nisi do veniam amet veniam quis. Deserunt quis amet consequat officia sint qui dolore voluptate. Laborum ad officia ullamco occaecat do minim.</p>
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
    const [searchInput, setSearchInput] = useState();
    const [nodeResponse, setNodeResponse] = useState();

    let lang = useContext(LanguageContext);
    let trans = i18n[lang];

    // Register event callback to update the state when content gets changed in Gentics Mesh    
    useWebsocketBridge(() => {
        if (searchInput && searchInput.query !== "") {
            filterExhibitions(lang, searchInput.query).then(setNodeResponse);
        } else {
            getExhibitions(lang, 1).then(setNodeResponse);
        }
    });

    useEffect(() => {
        if (searchInput && searchInput.query !== "") {
            filterExhibitions(lang, searchInput.query).then(setNodeResponse);
        } else {
            getExhibitions(lang, 1).then(setNodeResponse);
        }
    }, [lang, searchInput]);

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

    const resultList = results.length ? (results.filter(ex => {
        return ex.fields.title_image != null;
    }).map(exhibition => (
        <Exhibition exhibition={exhibition} key={exhibition.uuid} />
    ))) : emptyPlaceholder


    return (
        <div className="search-area">
            <Row>
                <Col lg={{ span: 4, offset: 4 }} className="text-center">
                    <Container>
                        <div className="search-input">
                            <input className="filter-input" type="text" placeholder="Filter" onChange={({ target: { value } }) => setSearchInput({ query: value })} />
                        </div>
                    </Container>
                </Col>
            </Row>
            <Row>
                {resultList}
            </Row>
        </div>
    )

}

function Exhibition({ exhibition }) {
    let lang = useContext(LanguageContext);
    let attribution = exhibition.fields.title_image.fields.attribution;
    let image = exhibition.fields.title_image;
    let color = image.fields.binary.dominantColor;
    return (
        <Col md={3} className="exhibit-item">
            <Link to={`/${lang}/exhibitions/${exhibition.fields.public_number}`} className="exhibit-link">
                <div className="exhibit-hover">
                    <div className="exhibit-hover-content">
                        <FontAwesomeIcon icon={faSearchPlus} className="fas fa-3x" />
                    </div>
                </div>
                <picture style={{ background: color }}>
                    <source media="(min-width: 320px)" srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=300&w=500&mode=smart&crop=fp`}></source>
                    <source media="(min-width: 786px)" srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=400&w=500&mode=smart&crop=fp`}></source>
                    <source media="(min-width: 1280px)" srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=500&w=500&mode=smart&crop=fp`}></source>
                    <img alt={exhibition.fields.name} srcSet={`${config.meshUrl}/musetech/webroot${image.path}?h=300&w=300&mode=smart&crop=fp`} className="img-responsive img-fluid" />
                </picture>
                <div className="image-attribution">
                    <p>{attribution}</p>
                </div>
            </Link>
            <div className="exhibit-caption">
                <p>{exhibition.fields.name}</p>
            </div>
        </Col>
    )
}
