import React from 'react';
import { Link } from 'react-router-dom';
import { usePromise, getExhibition } from '../api';
import Title from '../Title';
import Navigation from '../Navigation';

export default function ExhibitionView({ match }) {
    const id = match.params.id;
    const exhibitionsResponse = usePromise(() => getExhibition(id, "de"), [])
    if (!exhibitionsResponse) {
        return null;
    }

    return (
        <>
            <Title />
            <Navigation />
            <div className="padding-50">
                <div class="exhibit-list">
                    <div class="col-xs-12 col-sm-8 col-md-6">
                        <div class="exhibit-row-big">
                            <h2>
                                <Link to={`/exhibitions`}>&lt;&lt;</Link>&nbsp;{exhibitionsResponse.node.fields.title}</h2>
                            <img className="img-responsive" alt="" src={`/api/v1/demo/webroot${exhibitionsResponse.node.fields.images[0].node.path}?w=328`} />
                            <div className="info-text">{exhibitionsResponse.node.fields.description}
                            </div>
                            <br />
                            <Player data={exhibitionsResponse.node.fields.audio} />
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}


function Player({ data }) {
    if (data == null || data.path === null) {
        return "";
    }
    return (
        <audio controls>
            <source src={`/api/v1/demo/webroot${data.path}`} type="audio/mp3" />
            Your browser does not support the audio element.
            </audio>
    );
}