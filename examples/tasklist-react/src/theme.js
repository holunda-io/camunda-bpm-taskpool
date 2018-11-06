import { createMuiTheme } from '@material-ui/core/styles';
// import indigo from 'material-ui/colors/indigo';
// import pink from 'material-ui/colors/pink';
// import red from 'material-ui/colors/red';

import 'typeface-roboto';

export default createMuiTheme({
	palette: {
		primary: {
			main: '#3898EC'
		},
		secondary: {
			main: '#ff2d55'
		},
		overrides: {
			MuiButton: {
				root: {
					borderRadius: 2
				}
			},
			Paper: {
				root: {
					borderRadius: 0,
					padding: '8px 8px'
				}
			}
		}
	}
});
