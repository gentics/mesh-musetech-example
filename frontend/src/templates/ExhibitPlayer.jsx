import React, { useState, useEffect, useContext } from 'react';
import { getExhibit } from '../api';
import useWebsocketBridge from '../eventbus';
import { Col, Row } from 'react-bootstrap';
import { Route } from 'react-router-dom';
import LanguageContext from '../languageContext';
import LanguageToggle from '../components/LanguageToogle'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlay, faPause, faCircle } from '@fortawesome/free-solid-svg-icons'
import config from '../config.json';

const playerRef = React.createRef();
const playButton = React.createRef();

export default function ExhibitPlayer({ match }) {

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

    return (
        <>
            <div className="content exhibit-detail-caption" >
                <Row>
                    <Col lg={12} className="text-center">
                        <div className="exhibit-title">
                            <h2 className="section-heading">{exhibit.fields.name}</h2>
                        </div>
                    </Col>
                </Row>
                <Row className="text-center">
                    <Col lg={12}>
                        <PlayerIcons />
                    </Col>
                </Row>
                <Row>
                    <Col lg={12} className="text-center">
                        <div>
                            <h1><Route component={LanguageToggle} /></h1>
                        </div>
                    </Col>
                </Row>
                <Row>
                    <Col lg={12} className="text-center">
                        <div className="exhibit-detail-caption">
                            <Player data={exhibit.fields.audio} />
                        </div>
                    </Col>
                </Row>
                <Row className="bg-light">
                    <Col lg={12} className="text-center">
                        <p className="text-muted">{exhibit.fields.description}</p>
                    </Col>
                </Row>
            </div>
        </>
    );
}

function Player({ data }) {

    if (data == null || data.path === null) {
        return "";
    }

    return (
        <>
            <audio autoPlay={false} ref={playerRef} controls key={data.path}>
                <source src={`${config.meshUrl}/musetech/webroot${data.path}`} type="audio/mp3" />
                Your browser does not support the audio element.
            </audio>
        </>
    );
}

function PlayerIcons() {

    let lang = useContext(LanguageContext);

    const [playerState, setPlayerState] = useState();

    // Use effect hook to set the content when the path changes
    useEffect(() => {
        setPlayerState({ icon: faPlay, state: false });
    }, [lang]);

    if (!playerState) {
        return null;
    }

    return (
        <span className="fa-stack fa-6x" ref={playButton} onClick={togglePlayer}>
            <FontAwesomeIcon icon={faCircle} className="fas fa-stack-2x text-primary" />
            <FontAwesomeIcon icon={playerState.icon} className="fas fa-stack-1x fa-inverse" />
        </span>
    )

    function togglePlayer() {
        const newState = !playerState.state;
        if (newState) {
            playerRef.current.play();
            setPlayerState({ icon: faPause, state: true });
        } else {
            playerRef.current.pause();
            setPlayerState({ icon: faPlay, state: false });
        }
    }
}



