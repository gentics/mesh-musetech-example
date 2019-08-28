import React from 'react';
import logoFile from './logo.svg';

const logo = {
  src: logoFile,
  alt: 'MuseTech Museum',
};

export default function Title() {
  return (
    <div className="title row">
      <div className="col-md-4 col-sm-6">
        <a href="/"><img className="logo img-responsive" src={logo.src} alt={logo.alt} /></a>
      </div>
    </div>
  )
}
