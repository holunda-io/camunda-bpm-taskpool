import React, { Component } from "react";
import PropTypes from "prop-types";

import { AppInterface } from "./shared/interfaces";
import { AppSerive } from "./shared/services";

import "./App.scss";

//Material-UI Themeing import
import MuiThemeProvider from "@material-ui/core/styles/MuiThemeProvider";
import theme from "./theme";
import CssBaseline from "@material-ui/core/CssBaseline";

import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import AccountCircle from "@material-ui/icons/AccountCircle";
import Switch from "@material-ui/core/Switch";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import FormGroup from "@material-ui/core/FormGroup";
import MenuItem from "@material-ui/core/MenuItem";
import Menu from "@material-ui/core/Menu";

import Navbar from "./main-parts/navbar/navbar";
import TaskList from "./task-list/TaskList";
import { EmptyStateLoginPage } from "./shared/emptyState";
import classes from "*.module.scss";

const ApplicationContext: AppInterface[] = [
  {
    key: "aaa000",
    prozess: "Test-Prozess",
    taskName: "Eine unbestimmte Aufgabe",
    taskDescription: "Die Beschreibung der Aufgabe",
    creationDate: new Date(2018, 0, 13, 17, 25, 33),
    dueDate: new Date(2018, 9, 31, 0, 0, 0),
    modifiedAt: new Date(2018, 0, 13, 17, 25, 33),
    modifiedBy: new Date(2018, 0, 13, 17, 25, 33),
    priority: "normal",
    indicator: "group",
    candidateGroups: "",
    attributes: "",
    targetUrl: "",
    surce: ""
  }
];

export default class App extends Component {
  state = {
    auth: true
  };

  handleNavbarState = (childState: any) => {
    this.setState({
      auth: childState.auth
    });
  };

  render() {
    const { auth } = this.state;
    return (
      <MuiThemeProvider theme={theme}>
        <div>
          <CssBaseline />
          <Typography />
          <Navbar onUpdateNav={this.handleNavbarState} />
          {!auth ? (
            <React.Fragment>
              <TaskList />
            </React.Fragment>
          ) : (
            <EmptyStateLoginPage />
          )}
        </div>
      </MuiThemeProvider>
    );
  }
}
