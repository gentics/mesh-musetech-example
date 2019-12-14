import React from 'react';
import Navigation from '../components/Navigation';
import Footer from '../components/Footer';
import { useKeycloak } from 'react-keycloak';
import { Container } from 'react-bootstrap';

export default function SSOTest({ match }) {

    const [keycloak, initialized] = useKeycloak();

    return (
        <>
            <Navigation />
            <section className="page-section without-header">
                <Container>
                    <div>
                        <div>{`User is ${!keycloak.authenticated ? 'NOT ' : ''}authenticated`}</div>

                        {!keycloak.authenticated && (
                            <button type="button" onClick={() => keycloak.login()}>
                                Login
        </button>
                        )}

                        {!!keycloak.authenticated && (
                            <button type="button" onClick={() => keycloak.logout()}>
                                Logout
        </button>
                        )}
                    </div>
                    <div>Initialized: {` ${initialized} `}</div>
                </Container>
            </section>
            <Footer />
        </>
    );
}