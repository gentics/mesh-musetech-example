import React from 'react';
import Navigation from '../components/Navigation';
import Header from '../components/Header';
import Footer from '../components/Footer';
import { Container, Col, Row } from 'react-bootstrap';


export default function Imprint() {

    return (
        <>
            <Navigation />
            <Header lead="Imprint" heading="Imprint" className="abouthead" />

            <section className="page-section bg-light">

                <Container>
                    <Row>
                        <Col md={8} className="content col-md-offset-2">
                            <h2>Legal notice/disclosure</h2>
                            <p>
                                <a href="https://www.apa-it.at/Site/impressum/legal_notice_disclosure.PDF">Download</a>
                                <span></span>
                            </p>
                            <h2>APA-IT Informations Technologie GmbH</h2>
                            <p>Laimgrubengasse 10
                              <br />1060 Vienna
                              <br />Tel.: +43 1 36060-0
                              <br />Fax: +43 1 36060-6099
                              <br />E-Mail: it@apa.at
                              <br />Commercial register number: 195806a
                              <br />Commercial register court: Handelsgericht Wien
                              <br />VAT ID number: ATU53122400
                              <br />Data registry number: 4012664
                              <br />Member of the Austrian Federal Economic Chamber
                              <br />Legal form: Limited liability company
                            </p>
                            <p>
                              <img draggable="false" className="emoji" width={12} alt="©" src="https://twemoji.maxcdn.com/36x36/a9.png" /> APA - Austria Press Agency eG. All rights reserved.</p>
                            <p>
                              The entire contents are for personal information and private use only. Any form of copying, publishing or
                              making this content or parts of this content available to others, the transmission of, or entry into,
                              electronic databases is strictly prohibited. 
                              APA has the exclusive right to authorise permission to use the content in any other way. APA does not take any
                              responsibility for completeness and accuracy of this information.
                            </p>
                            <p>
                              If you are interested in additional usage, please contact&nbsp;<a href="mailto:mesh@gentics.com">Gentics</a>.
                            </p>
                            <p>
                              APA is a registered Austrian trademark and a European Community trademark.
                            </p>
                        </Col>
                    </Row>
                </Container>

            </section>
            <Footer />
        </>
    )
}
