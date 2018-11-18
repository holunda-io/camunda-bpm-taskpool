import * as React from "react";

import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import {Table, TableBody, TableCell, TableHead, TableRow} from "@material-ui/core";

import {createStyles, withStyles} from "@material-ui/core/styles";
import {Theme} from "@material-ui/core/styles/createMuiTheme";

import {EmptyStateTaskList} from "../shared/emptyState";
import {TaskItem} from "../shared/interfaces";

export interface ITaskListProps {
  classes: any;
}
export interface ITaskListState {
  tasksAvailable: boolean;
  taskItems: TaskItem[];
}

const styles = (theme: Theme) =>
  createStyles({
    thead: {
      fontSize: "1.8rem"
    }
  });

class TaskList extends React.Component<ITaskListProps, ITaskListState> {
  constructor(props: ITaskListProps) {
    super(props);
    this.state = {
      tasksAvailable: false,
      taskItems: []
    };
  }

  componentDidMount() {
    fetch('http://localhost:3000/example-tasklist.json')      // returns a promise object
      .then(result => result.json()) // still returns a promise object, U need to chain it again
      .then(items => {

        this.setState({
          taskItems: items,
          tasksAvailable: true
        });
      });
  }

  public render() {
    const {classes} = this.props;

    const taskListTable = (
      <React.Fragment>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell className={classes.thead}>Process</TableCell>
              <TableCell className={classes.thead}>Name</TableCell>
              <TableCell className={classes.thead}>Details</TableCell>
              <TableCell className={classes.thead}>Created</TableCell>
              <TableCell className={classes.thead}>Due</TableCell>
              <TableCell className={classes.thead}>Prio</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {this.state.taskItems.map(taskItem => {
              return (
                <TableRow key={taskItem.task.id}>
                  <TableCell component="th" scope="row">{taskItem.task.processName}</TableCell>
                  <TableCell component="th" scope="row">{taskItem.task.name}</TableCell>
                  <TableCell component="th" scope="row">{taskItem.task.description}</TableCell>
                  <TableCell component="th" scope="row">{taskItem.task.createTime}</TableCell>
                  <TableCell component="th" scope="row">{taskItem.task.dueDate}</TableCell>
                  <TableCell component="th" scope="row">{taskItem.task.priority}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </React.Fragment>
    );

    return (
      <React.Fragment>
        <Grid container spacing={24} className="base-grid-container">
          <Grid item xs={12}>
            <Paper>
              {this.state.tasksAvailable ? (
                taskListTable
              ) : (
                <EmptyStateTaskList/>
              )}
            </Paper>
          </Grid>
        </Grid>
      </React.Fragment>
    );
  }
}

export default withStyles(styles)(TaskList);
