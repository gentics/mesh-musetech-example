import React from 'react';
import { Link } from 'react-router-dom';
import { usePromise, getExhibitions } from '../api';
import Title from '../Title';
import Navigation from '../Navigation';

export default function ExhibitionsList() {
    const exhibitionsResponse = usePromise(() => getExhibitions(), [])
    if (!exhibitionsResponse) {
        return null;
    }

    const list = exhibitionsResponse.node;

    return (
        <>
            <Title />
            <Navigation />
            <div className="content">
                <div>
                    <h1>Exhibitions</h1>
                    <div className="row">
                        <div className="exhibit-list">
                            {list.children.elements.filter(ex => {
                                return ex.fields.audio != null
                            }).map(exhibition => (
                                <Exhibition exhibition={exhibition} key={exhibition.uuid} />
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}


function Exhibition({ exhibition }) {
    /*
    const color = exhibition.fields.images[0].node.fields.binary.dominantColor;
    const divStyle = {
        'background-color': color,
    };
    */
    return (
        <div className="col-xs-12 col-sm-6 col-md-3">
            <div className="exhibit-row">
                <div className="image-container">
                    <Link to={`/exhibitions/${exhibition.fields.public_number}`}>
                        <img alt="" className="img-responsive" src={`/api/v1/demo/webroot${exhibition.fields.images[0].node.path}`} />
                    </Link>
                </div>
                <div className="exhibit-title">
                    <h2 className="exhibit-title">{exhibition.fields.title}</h2>
                </div>
            </div>
        </div>
    )
}
