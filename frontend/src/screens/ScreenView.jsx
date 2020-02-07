import React, { useState, useEffect } from 'react';
import Slider from "react-slick";
import { getScreen } from '../api';
import useWebsocketBridge from '../eventbus';
import Title from '../components/Title';
import { Row } from 'react-bootstrap';
import config from '../config.json';
import '../css/screen.css';

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

function InfoSelector({ content }) {
    const type = content.type;
    if (type === "ScreenExhibitPromo") {
        return (
            <div className="image-label"><h2>{content.fields.title}</h2></div>
        );
    } else if (type === "ScreenEvent") {
        return (
            <div className="image-label">
                <h2>{content.fields.title}</h2>
                <p className="label label-default">Start: {content.fields.tour.fields.dates[0].fields.date}</p>
                <br />
                <p className="label label-default">Seats: {content.fields.tour.fields.dates[0].fields.seats}</p>
                <br />
                <p className="label label-default">Duration: {content.fields.tour.fields.duration}min</p>
                <br />
                <p className="label label-default">Location: {content.fields.tour.fields.location}</p>
            </div>
        );
    } else {
        console.warn("Type {" + type + "} unknown.");
        return null;
    }
}

function MediaSelector({ content }) {
    console.dir(content);
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

