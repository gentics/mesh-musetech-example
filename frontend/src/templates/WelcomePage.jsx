import React, { useContext, useState, useEffect } from 'react';
import Navigation from '../components/Navigation';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { Container, Col, Row } from 'react-bootstrap';
import LanguageContext from '../languageContext';
import DOMPurify from 'dompurify';
import { loadAdmissionInfo } from '../api';
import config from '../config.json';

const i18n = {
  en: {
    disclaimer: "This site represents a <i>#musetech</i> demo which was build as a showcase example.<br/> Feel free to explore this site, audio guide, digital signage area and the <a href=\"https://getmesh.io/blog/muse-tech-2\">corresponding blogposts</a>.<br/><br/> You can reach us on <a href=\"https://twitter.com/genticsmesh/\">Twitter</a>, <a href=\"https://gitter.im/gentics/mesh\">Gitter Chat</a> of via <a href=\"mailto:mesh@gentics.com?subject=musetech.getmesh.io\">Mail</a> if you have questions about the demo and how a headless CMS can empower your content.",
    from: "From",
    hours: "Our hours",
    admission: "Admission",
    to: "To"
  },
  de: {
    disclaimer: "Diese Seite stellt eine #Musetech Demo dar. Sie enthält eine Auswahl von Austellungen, einen Audio Guide und einen Digital Signage Bereich. Den dazugehörigen Blogpost können sie hier <a href=\"https://getmesh.io/blog/muse-tech-2\">lesen</a> um mehr über die Demo zu erfahren. <br/><br/> Sie können uns ausserdem auf <a href=\"https://twitter.com/genticsmesh/\">Twitter</a>, <a href=\"https://gitter.im/gentics/mesh\">Gitter Chat</a> oder via <a href=\"mailto:mesh@gentics.com?subject=musetech.getmesh.io\">Mail</a> erreichen wenn Sie Fragen zu dieser Demo haben.",
    from: "Von",
    hours: "Unsere Öffnungszeiten",
    admission: "Preise",
    to: "Bis"
  }
}

export default function WelcomeScreen({ content }) {

  const [admissionInfo, setAdmissionInfo] = useState();
  let lang = useContext(LanguageContext);

  useEffect(() => {
    loadAdmissionInfo(lang).then(setAdmissionInfo);
  }, [lang]);

  let trans = i18n[lang];
  let admissionInfoTypes = [];
  if (admissionInfo) {
    admissionInfoTypes = admissionInfo.node.fields.types;
  }


  return (
    <>
      <Navigation languages={content.languages} />
      <VideoHeader src={`/video/header.webm`} lead={content.fields.title} heading={content.fields.intro} />

      <section className="page-section bg-light">
        <Container>
          <Row>
            <Col lg={{ span: 8, offset: 2 }} className="text-center">
              <span className="text-muted" dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(trans.disclaimer) }}></span>
            </Col>
          </Row>
        </Container>
      </section>

      <section className="bg-light page-section no-padding" id="info">
        <Container>
          <Row>
            <Col lg={{ span: 2, offset: 2 }} className="info-left">
              <h3 className="section-subheading text-muted">
                {trans.admission}:<br /><br />
                {admissionInfoTypes.map((info, index) => <AdmissionInfo index={index} key={info.version + info.uuid} content={info} />)}
              </h3>
            </Col>
            <Col lg={4} className="text-center">
              <h3 className="section-subheading text-muted">
                {content.fields.museum.fields.name}<br />
                {content.fields.museum.fields.street}<br />
                {content.fields.museum.fields.city}<br />
                <br />
                {content.fields.museum.fields.email}<br />
                {content.fields.museum.fields.phone}<br />
              </h3>
            </Col>
            <Col lg={4} className="info-left">
              <h3 className="section-subheading text-muted">
                {trans.hours}:<br /><br />
                {content.fields.openinghours.map((section, index) => <HoursEntry index={index} key={content.version + section.uuid} content={section} />)}
              </h3>
            </Col>
          </Row>
        </Container>
      </section>
      <Footer />
    </>
  );
}

function AdmissionInfo({ content, index }) {
  return (
    <div>
      <strong>{content.fields.title}</strong>: {content.fields.price}
    </div>
  )
}

function HoursEntry({ content, index }) {
  return (
    <div>
      <strong>{content.fields.days}</strong>: {content.fields.from} - {content.fields.to}
    </div>
  )
}

function VideoHeader({ src, lead, heading }) {
  let url = config.meshUrl + "/musetech/webroot/" + src;
  return (
    <>
      <div>
        <div className="headvideo">
          <video
            autoPlay
            muted
            width="100%"
            loop={true}>
            <source src={url}>
            </source>
          </video>
        </div>
        <Header lead={lead} heading={heading} className="masthead" />
      </div>
    </>
  )

}