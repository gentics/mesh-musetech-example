import React from 'react';
import {createSharedState} from 'react-hook-shared-state'

const [useState, setState] = createSharedState({counter1: 42, counter2:41});
export default function Counter() { 
    const state = useState();    
    return (
    <div>
        Count: {state.counter1} &nbsp;
        <button onClick={ () => setState({...state, counter1: state.counter1+1}) } >+</button>
    </div>
    );
}

export function Counter2() { 
    const state = useState();
    return (
    <div>
        Count: {state.counter2} &nbsp;
        <button onClick={() => setState({counter2: state.counter2+1,  counter1: state.counter1+1}) } >+</button>
    </div>
    );
}



