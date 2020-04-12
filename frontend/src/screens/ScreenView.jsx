import React, { useState, useEffect } from 'react';
import Slider from "react-slick";
import { getScreen } from '../api';
import useWebsocketBridge from '../eventbus';
import Title from '../components/Title';
import { Col, Row, Container } from 'react-bootstrap';
import config from '../config.json';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClock, faMapMarker, faCalendarDay, faUserFriends } from '@fortawesome/free-solid-svg-icons'
import '../css/screen.css';
import { isToday, isTomorrow, lightFormat } from 'date-fns';
import { utcToZonedTime } from 'date-fns-tz';
import { isBefore } from 'date-fns/esm';

const timeZone = 'Europe/London';

export default function ScreenView({ match }) {
    const id = match.params.id;

    const [screenResponse, setScreenResponse] = useState();
    useWebsocketBridge(async () => { setScreenResponse(await getScreen(id)) });
    useEffect(() => {
        getScreen(id).then(setScreenResponse);
    }, [id]);

    if (!screenResponse) {
        return null;
    }

    return (
        <>
            <Title />
            <Row>
                <SimpleSlider node={screenResponse.node} />
            </Row>
        </>
    )
}

function SimpleSlider({ node }) {

    var settings = {
        dots: false,
        autoplay: true,
        autoplaySpeed: 10000,
        infinite: true,
        arrows: false,
        slidesToShow: 1,
        speed: 500
    };
    return (
        <div className="container padding-50">
            <Slider {...settings}>
                {node.fields.contents.map((content, idx) => (
                    <div key={`${node.uuid}@${node.version}-${idx}`}>
                        <div className="image-box">
                            <MediaSelector content={content} />
                            <InfoSelector content={content} />
                        </div>
                    </div>
                ))}
            </Slider>
        </div>
    );

}

function findLatestDate(tourDates) {
    /*
    .filter(tourDate => {
        let seats = tourDate.fields.seats;
        return seats !== null && seats !== 0;
    })
    */
    let dates = tourDates.map(tourDate => {
        tourDate.utcDate = utcToZonedTime(tourDate.fields.date, timeZone);
        return tourDate;
    }).filter(tourDate => {
        return isBefore(new Date(), tourDate.utcDate);
    }).map(tourDate => {
        let dateStr = lightFormat(tourDate.utcDate, "dd.MM.yyyy HH:mm");
        if (isToday(tourDate.utcDate)) {
            dateStr = "Today " + lightFormat(tourDate.utcDate, "HH:mm");
        } else if (isTomorrow(tourDate.utcDate)) {
            dateStr = "Tomorrow " + lightFormat(tourDate.utcDate, "HH:mm");
        }
        tourDate.dateStr = dateStr;
        return tourDate;
    }).sort(function (a, b) {
        return !isBefore(a.utcDate, b.utcDate);
    });

    return dates[0];
}

function InfoSelector({ content }) {
    const type = content.type;
    if (type === "ScreenExhibitPromo") {
        return (
            <div className="image-label"><h2>{content.fields.title}</h2></div>
        );
    } else if (type === "ScreenEvent") {

        let latestDate = findLatestDate(content.fields.tour.fields.dates);

        return (
            <div className="image-label">

                <Container>
                    <Row>
                        <Col md={12} className="text-center">
                            <h2>{content.fields.tour.fields.title}</h2>
                        </Col>
                    </Row>
                    <Row className="label label-default">
                        <Col md={1} className="text-center">
                            <FontAwesomeIcon icon={faCalendarDay} className="fas fa-2x" />
                        </Col>
                        <Col md={10} className="text-center">
                            <h4>{latestDate.dateStr}</h4>
                        </Col>
                    </Row>

                    <Row>
                        <Col md={12} className="text-center">
                            <br />
                        </Col>
                    </Row>

                    <Row className="label label-default">
                        <Col md={1} className="text-center">
                            <FontAwesomeIcon icon={faUserFriends} className="fas fa-2x" />
                        </Col>
                        <Col md={10} className="text-center">
                            <h3>{latestDate.fields.seats} available</h3>
                        </Col>
                    </Row>

                    <Row>
                        <Col md={12} className="text-center">
                            <br />
                        </Col>
                    </Row>

                    <Row className="label label-default">
                        <Col md={1} className="text-center">
                            <FontAwesomeIcon icon={faClock} className="fas fa-2x" />
                        </Col>
                        <Col md={10} className="text-center">
                            <h3>{content.fields.tour.fields.duration}min</h3>
                        </Col>
                    </Row>

                    <Row>
                        <Col md={12} className="text-center">
                            <br />
                        </Col>
                    </Row>

                    <Row className="label label-default">
                        <Col md={1} className="text-center">
                            <FontAwesomeIcon icon={faMapMarker} className="fas fa-2x" />
                        </Col>
                        <Col md={10} className="text-center">
                            <h3>{content.fields.tour.fields.location}</h3>
                        </Col>
                    </Row>
                </Container>
            </div>
        );
    } else {
        console.warn("Type {" + type + "} unknown.");
        return null;
    }
}

function MediaSelector({ content }) {
    if (content.fields.video != null) {
        return (
            <VideoPlayer video={content.fields.video} />
        );
    } else {
        return (
            <img src={`${config.meshUrl}/musetech/webroot${content.fields.image.node.path}?w=1200`} alt="" />
        );
    }
}

function VideoPlayer({ video }) {
    if (video == null || video.node.path === null) {
        return null;
    }
    return (
        <video
            muted
            autoPlay
            loop={true}
            width="100%">
            <source src={`${config.meshUrl}/musetech/webroot${video.node.path}`}></source>
            Sorry, your browser doesn't support embedded videos.
        </video>
    );
}

