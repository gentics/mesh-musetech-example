import React, { useState, useEffect } from 'react';
import { loadContentByPath } from './api';
import useWebsocketBridge from './eventbus';
import History from './templates/HistoryPage';
import ContentPage from './contentpage/ContentPage';
import WelcomePage from './templates/WelcomePage';
import AdmissionPage from './templates/AdmissionPage';
import NoMatch from './templates/NoMatch';

export default function DynamicContent({ match }) {
    let path = match.params.path;

    // Create state for the component
    const [content, setContent] = useState();

    // Register event callback to update the state when content gets changed in Gentics Mesh
    useWebsocketBridge(() => {
        loadContentByPath(path).then(setContent);
    });

    // Use effect hook to set the content when the path changes
    useEffect(() => {
        loadContentByPath(path).then(setContent);
    }, [path]);

    if (content === undefined) {
        return null;
    }
    
    // content is null when graphql did not find a node - show a 404 message
    if (content === null) {
        return <NoMatch />;
    }
    let schemaName = content.schema.name;
    switch (schemaName) {
        case "HistoryPage":
            return <History content={content} />
        case "ContentPage":
            return <ContentPage content={content} />
        case "WelcomePage":
            return <WelcomePage content={content} />
        case "AdmissionPage":
            return <AdmissionPage content={content} />
        default:
            return <NoMatch />;
    }
}