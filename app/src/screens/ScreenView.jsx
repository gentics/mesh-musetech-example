import React, { useState, useEffect } from 'react';
import Slider from "react-slick";
import { getScreen } from '../api';
import useWebsocketBridge from '../eventbus';
import Title from '../Title';

export default function ScreenView({ match }) {
    const id = match.params.id;

    const [screenResponse, setScreenResponse] = useState();
    useWebsocketBridge(async () => { setScreenResponse(await getScreen(id)) });
    useEffect(() => {
        getScreen(id).then(setScreenResponse);
    }, []);

    if (!screenResponse) {
        return null;
    }


    return (
        <>
            <Title />
            <div className="row">
                <SimpleSlider node={screenResponse.node} />
            </div>
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
    if (type === "ScreenExhibitionPromo") {
        return (
            <div className="image-label"><h2>{content.title}</h2></div>
        );
    } else if (type === "ScreenEvent") {
        return (
            <div className="image-label">
                <h2>{content.title}</h2>
                <p className="label label-default">Start: {content.start}</p>
                <br />
                <p className="label label-default">Dauer: {content.duration}min</p>
                <br />
                <p className="label label-default">Ort: {content.location}</p>
            </div>
        );
    } else {
        console.warn("Type {" + type + "} unknown.");
        return null;
    }
}

function MediaSelector({ content }) {
    console.dir(content);
    if (content.video != null) {
        return (
            <VideoPlayer video={content.video} />
        );
    } else {
        return (
            <img src={`/api/v1/demo/webroot${content.image.node.path}`} alt="" />
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
            width="100%">
            <source src={`/api/v1/demo/webroot${video.node.path}`}></source>
            Sorry, your browser doesn't support embedded videos.
        </video>
    );
}

