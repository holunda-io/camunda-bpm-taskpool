import * as React from 'react';

import { AppInterface } from './interfaces';

const ctxt = React.createContext<AppInterface | null>(null);

export const AppContextProvider = ctxt.Provider;
export const AppContextConsumer = ctxt.Consumer;

export class AppSerive extends React.Component<AppInterface, any> {
	state = {};
	public render() {
		return (
			<div>
				<p />
			</div>
		);
	}
}
