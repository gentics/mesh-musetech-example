import React, { useState, useEffect, useContext } from 'react';
import { getTour } from '../api';
import { Link } from 'react-router-dom';
import Navigation from '../components/Navigation';
import Footer from '../components/Footer';
import useWebsocketBridge from '../eventbus';
import { Col, Row, Container } from 'react-bootstrap';
import LanguageContext from '../languageContext';
import config from '../config.json';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClock, faMapMarker, faUserFriends, faEuroSign } from '@fortawesome/free-solid-svg-icons'
import { parseISO, isToday, isTomorrow, lightFormat } from 'date-fns';

const trans = {
    de: {
        seats: "Plätze",
        price: "Preis",
        your: "Deine",
        today: "Heute",
        tomorrow: "Morgen",
        notToday: "Am",
        free: "Freie",
        dates: "Termine"
    },
    en: {
        seats: "seats",
        price: "Price",
        your: "Your",
        today: "Today",
        tomorrow: "Tomorrow",
        notToday: "On",
        free: "Free",
        dates: "Dates"
    }
}
export default function TourView({ match }) {
    // Create state for the component
    const [tour, setTour] = useState();
    const id = match.params.id;
    let lang = useContext(LanguageContext);
    const i18n = useI18n();

    // Register event callback to update the state when content gets changed in Gentics Mesh
    useWebsocketBridge(() => {
        getTour(id, lang).then(setTour);
    });

    // Use effect hook to set the content when the path changes
    useEffect(() => {
        getTour(id, lang).then(setTour);
    }, [id, lang]);

    if (!tour) {
        return null;
    }

    const guides = tour.fields.guides.filter(guide => {
        return guide != null;
    }).map(guide => (
        <Guide guide={guide.node} key={guide.uuid} />
    ));

    const dates = tour.fields.dates.filter(date => {
        return date != null;
    }).map(date => (
        <TourDate date={date} key={date.uuid} />
    ));

    return (
        <>
            <Navigation />
            <section className="page-section without-header">
                <Container>
                    <div className="content tour-detail-caption bg-light" >
                        <Row>
                            <Col lg={12} className="text-center">
                                <div className="tour-title">
                                    <h2 className="section-heading"><Link to={`/${lang}/tours`}>&lt;&lt;</Link>&nbsp;{tour.fields.title}</h2>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg={{ span: 6, offset: 3 }}>
                                <Container>
                                    <Row>
                                        <Col lg={12}>
                                            <picture>
                                                <source media="(min-height: 320px)" srcSet={`${config.meshUrl}/musetech/webroot${tour.fields.image.path}?w=500&mode=smart`}></source>
                                                <source media="(min-height: 786px)" srcSet={`${config.meshUrl}/musetech/webroot${tour.fields.image.path}?w=800&mode=smart`}></source>
                                                <source media="(min-height: 1280px)" srcSet={`${config.meshUrl}/musetech/webroot${tour.fields.image.path}?w=1200&mode=smart`}></source>
                                                <img alt={tour.fields.name} srcSet={`${config.meshUrl}/musetech/webroot${tour.fields.image.path}?w=600&mode=smart`} className="img-responsive img-fluid" />
                                            </picture>
                                            <div className="image-attribution">
                                                <p>{tour.fields.image.fields.attribution}</p>
                                            </div>
                                        </Col>
                                    </Row>

                                    <Row>
                                        <Col lg={12}>
                                            <br />
                                        </Col>
                                    </Row>

                                    <Row>
                                        <Col md={{ span: 1, offset: 3 }} className="text-center">
                                            <FontAwesomeIcon icon={faUserFriends} className="fas fa-1x" />
                                        </Col>
                                        <Col md={{ span: 3, offset: 0 }} className="text-left">
                                            <p>max. {tour.fields.size}</p>
                                        </Col>

                                        <Col md={{ span: 1, offset: 0 }} className="text-center">
                                            <FontAwesomeIcon icon={faClock} className="fas fa-1x" />
                                        </Col>
                                        <Col md={{ span: 2, offset: 0 }} className="text-left">
                                            <p>{tour.fields.duration}min</p>
                                        </Col>
                                    </Row>
                                    <Row>
                                        <Col md={{ span: 1, offset: 3 }} className="text-center">
                                            <FontAwesomeIcon icon={faEuroSign} className="fas fa-1x" />
                                        </Col>
                                        <Col md={{ span: 3, offset: 0 }} className="text-left">
                                            <p>{tour.fields.price} €</p>
                                        </Col>

                                        <Col md={{ span: 1, offset: 0 }} className="text-center">
                                            <FontAwesomeIcon icon={faMapMarker} className="fas fa-1x" />
                                        </Col>
                                        <Col md={{ span: 3, offset: 0 }} className="text-left">
                                            <p>{tour.fields.location}</p>
                                        </Col>
                                    </Row>
                                </Container>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg={{ span: 8, offset: 2 }} className="text-center">
                                <div className="tour-detail-caption">
                                    <p className="text-muted">{tour.fields.description}</p>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col lg={{ span: 8, offset: 2 }} className="text-center">
                                <div className="exhibit-detail-caption">
                                    <h2 className="text-muted">{i18n.your} Guides</h2>
                                    {guides}
                                </div>
                            </Col>
                        </Row>

                        <Row>
                            <Col lg={{ span: 8, offset: 2 }} className="text-center">
                                <div className="exhibit-detail-caption">
                                    <h2 className="text-muted">{i18n.dates}</h2>
                                    {dates}
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

function TourDate({ date }) {
    let tourDate = parseISO(date.fields.date);
    const i18n = useI18n();
    let prefix = datePrefix(tourDate, i18n);

    return (
        <Container className="border rounded tour-date">
            <Row>
                <Col lg={{ span: 8, offset: 0 }} className="text-center">
                    {/* - In {formatDistanceToNow(tourDate, { locale: locales[lang] })} */}
                    {prefix} {lightFormat(tourDate, "dd.MM.yyyy HH:mm")}
                </Col>
                <Col lg={{ span: 4, offset: 0 }} className="text-center">
                {i18n.free} {i18n.seats}: {date.fields.seats} 
                </Col>
            </Row>
        </Container>
    )
}

function Guide({ guide }) {
    return (
        <Container className="tour-guide border rounded">
            <Row>
                <Col lg={{ span: 4, offset: 0 }}>
                    <picture>
                        <source media="(min-height: 320px)" srcSet={`${config.meshUrl}/musetech/webroot${guide.fields.image.path}?w=200&mode=smart`}></source>
                        <source media="(min-height: 786px)" srcSet={`${config.meshUrl}/musetech/webroot${guide.fields.image.path}?w=300&mode=smart`}></source>
                        <source media="(min-height: 1280px)" srcSet={`${config.meshUrl}/musetech/webroot${guide.fields.image.path}?w=300&mode=smart`}></source>
                        <img alt={guide.fields.firstname} srcSet={`${config.meshUrl}/musetech/webroot${guide.fields.image.path}?w=300&mode=smart`} className="img-responsive img-fluid" />
                    </picture>
                </Col>
                <Col lg={{ span: 8, offset: 0 }} className="text-center">
                    <p className="text-muted">{guide.fields.firstname} {guide.fields.lastname}</p>
                    <p><a href={`mailto:${guide.fields.email}`}>{guide.fields.email}</a></p>
                    <p className="text-muted guide-quote">"{guide.fields.quote}"</p>

                </Col>
            </Row>
        </Container>
    )
}

function useI18n() {
    let lang = useContext(LanguageContext);
    let i18n = trans[lang];
    return i18n;
}

function datePrefix(tourDate, i18n) {
    let today = isToday(tourDate);
    let tomorrow = isTomorrow(tourDate);
    if (today) {
        return i18n.today;
    }
    if (tomorrow) {
        return i18n.tomorrow;
    }
    return i18n.notToday;
}

