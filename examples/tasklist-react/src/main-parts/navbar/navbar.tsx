import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles, createStyles } from '@material-ui/core/styles';
import { WithStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import AccountCircle from '@material-ui/icons/AccountCircle';
import { Theme } from '@material-ui/core/styles/createMuiTheme';

import theme from '../../theme';

const styles = (theme: Theme) =>
	createStyles({
		root: {
			marginTop: theme.spacing.unit * 3,
			width: '100%'
		},
		flex: {
			flex: 1
		},
		menuButton: {
			marginLeft: -12,
			marginRight: 20,
			fontSize: '1.6rem'
		},
		menu: {
			marginLeft: 'auto',
			marginRight: 20
		}
	});

// class Navbar extends Component<WithStyles<typeof styles>, any> {
class Navbar extends Component<any, any> {
	constructor(props: any) {
		super(props);
	}
	state = {
		auth: false,
		anchorElement: null
	};

	handleUserMenu = (event: any) => {
		this.setState({ anchorElement: event.currentTarget });
	};

	handleProfile = (event: any) => {
		console.log('Navbar handle profile: ', event.target.value);
	};
	handleAccount = (event: any) => {
		console.log('navbar handle account: ', event.target.value);
	};

	handleLoginLogout = () => {
		this.setState({ auth: !this.state.auth });
		this.sendStateToParent(this.state);
	};

	handleClose = () => {
		this.setState({ anchorElement: null });
	};

	sendStateToParent = (state: any) => {
		this.props.onUpdateNav(state);
	};
	render() {
		const { classes } = this.props;
		const { auth, anchorElement } = this.state;
		const open = Boolean(anchorElement);

		return (
			<AppBar position="sticky">
				<Toolbar>
					<IconButton color="inherit" aria-label="Menu">
						<MenuIcon />
					</IconButton>

					<Typography variant="title" color="inherit" className={classes.flex}>
						Holisticon TaskList
					</Typography>

					{auth && (
						<div>
							<IconButton
								aria-owns={auth ? 'menu-appbar' : undefined}
								className={classes.menuButton}
								aria-haspopup="true"
								onClick={this.handleUserMenu}
								color="inherit"
							>
								<AccountCircle />
							</IconButton>
							<Menu
								id="app-bar"
								anchorEl={anchorElement}
								anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
								transformOrigin={{ vertical: 'top', horizontal: 'right' }}
								open={open}
								onClose={this.handleClose}
								className={classes.menu}
							>
								<MenuItem onClick={this.handleAccount}>Account</MenuItem>
								<MenuItem onClick={this.handleProfile}>Profile</MenuItem>
								<MenuItem onClick={this.handleLoginLogout}>Logout</MenuItem>
							</Menu>
						</div>
					)}

					{!auth && (
						<div>
							<Button onClick={this.handleLoginLogout} className={classes.menuButton} color="inherit">
								Login
							</Button>
						</div>
					)}
				</Toolbar>
			</AppBar>
		);
	}
}

// Navbar.propTypes = {
// 	classes: PropTypes.object.isRequired
// };

export default withStyles(styles)(Navbar);
