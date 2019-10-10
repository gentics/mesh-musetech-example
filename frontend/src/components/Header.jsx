import React from 'react';

export default function Title({className, lead, heading}) {
    return (
        <header className={className}>
            <div className="container">
                <div className="intro-text fade-in">
                    <div className="intro-lead-in">{lead}</div>
                    <div className="intro-heading text-uppercase">{heading}</div>
                </div>
            </div>
        </header>
    )
}