import React, { useState, useEffect } from 'react';
import { Col, Container, Row } from 'react-bootstrap';
import CookieConsent, { Cookies } from "react-cookie-consent";
import ReactGA from 'react-ga';

let consentCookie = Cookies.get("CookieConsent");

export default function Footer() {

    const [consent, setConsent] = useState();
    if (consent === undefined && consentCookie === "true") {
        setConsent(true);
    }

    useEffect(() => {
        if (consent === true) {
            //console.log("Accepted - Enabling GA");
            enableGA();
        }
    }, [consent]);

    return (
        <>
            <footer className="footer">
                <Container>
                    <Row className="align-items-center">
                        <Col md={4}>
                            <span className="copyright">BMH - 2020</span>
                        </Col>
                        <Col md={4}>
                            <Container>
                                <Row className="align-items-center">
                                    <Col md={12}>
                                        <span className="copyright">This MuseTech Demo is powered by </span>
                                    </Col>
                                    <Col md={12}>
                                        <a href="https://getmesh.io"><div className="promo" /></a>
                                    </Col>
                                </Row>
                            </Container>
                        </Col>
                        <Col md={4}>
                            <ul className="list-inline quicklinks">
                                <li className="list-inline-item">
                                    <a href="/en/imprint">Imprint</a>
                                </li>
                            </ul>
                        </Col>
                    </Row>
                </Container>
            </footer>
            <CookieConsent
                enableDeclineButton
                buttonText="I accept"
                onAccept={acceptConsent}>
                We request your consent for cookies. We use technically necessary, functional and marketing cookies. Marketing cookies are only used with your consent and exclusively for statistical purposes; our websites are free of advertisement. Except for the technically necessary cookies, all cookies are disabled from the outset.
        </CookieConsent>
        </>
    )

    function acceptConsent() {
        setConsent(true);
    }

    function enableGA() {
        ReactGA.initialize('UA-147567542-1');
        ReactGA.pageview(window.location.pathname + window.location.search);
    }
}

