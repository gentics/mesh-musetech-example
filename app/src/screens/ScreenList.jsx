import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getScreens } from '../api';
import useWebsocketBridge from '../eventbus';
import Title from '../Title';
import Navigation from '../Navigation';
export default function ScreenList() {
const [screensResponse, setScreensResponse] = useState();
useWebsocketBridge(async () => { setScreensResponse(await getScreens()) });
useEffect(() => {
getScreens().then(setScreensResponse);
}, []);
if (!screensResponse) {
return null;
}
const folder = screensResponse.node;
return (
<>
  <Title />
  <Navigation />
  <div className="content">
    <h1>Screens</h1>
    <div className="row">
      {folder.children.elements.map(screen => (
      <Screen screen={screen} key={screen.uuid} />
      ))}
    </div>
  </div>
</>
);
}
function Screen({ screen }) {
return (
<div class="col-xs-12 col-sm-6 col-md-4">
  <div class="exhibit-row">
    <h3>
      <Link to={`/screens/${screen.fields.id}`}>{screen.fields.name} </Link>
    </h3>
    <p className="description">{screen.fields.description}</p>
    <div className="row">
      <div className="col-xs-6">
        <span className="label label-default">Slides: {screen.fields.contents.length}</span>
        <br />
        <span className="label label-default">Location: {screen.fields.location}</span>
      </div>
    </div>
  </div>
</div>
)
}