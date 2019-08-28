import React, { useRef, useState, useEffect } from 'react';
import { getVideos } from '../api';
import {useWebsocketESPBridge} from '../eventbus';
import Title from '../Title';
import Navigation from '../Navigation';

export default function VideoList() {
  const [videosResponse, setVideosResponse] = useState();
  useWebsocketESPBridge(async () => { setVideosResponse(await getVideos()) });
  useEffect(() => {
    getVideos().then(setVideosResponse);
  }, []);

  if (!videosResponse) {
    return null;
  }
  const folder = videosResponse.node;
  return (
    <>
      <Title />
      <Navigation />
      <div className="content">
        <div className="product-list">
          <h1>Videos</h1>
          <div className="row">
            {folder.children.elements.map(video => (
              <Video video={video} key={video.uuid} />
            ))}
          </div>
        </div>
      </div>
    </>
  );
}

function Video({ video }) {
  const videoEl = useRef(null);
  useWebsocketESPBridge(() => { 
    if(videoEl.current.paused) {
      videoEl.current.play();
    } else {
      videoEl.current.pause();
    }
  });

  return (
    <div className="col-xs-12 col-sm-6 col-md-4">
      <div className="exhibit-row">
        <h4>{video.fields.filename}</h4>
        <video controls muted
          src={`/api/v1/demo/webroot${video.path}`}
          width="100%"

          ref={videoEl}>
          Sorry, your browser doesn't support embedded videos.
            </video>
        <br />
        <p className="description">{video.fields.description}</p>
      </div>
    </div>
  )
}

