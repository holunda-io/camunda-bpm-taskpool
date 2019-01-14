import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApprovalRequest } from 'process/model/approvalRequest';
import { ApproveRequestService } from 'process/api/approveRequest.service';
import { Task } from 'process/model/task';
import { TaskApproveRequestSubmitData } from 'process/model/taskApproveRequestSubmitData';

@Component({
  selector: 'app-task-approve',
  templateUrl: './approve-task.component.html',
  styleUrls: [ '../tasks.component.scss' ]
})
export class ApproveTaskComponent {

  private taskId: string;

  task: Task = this.emptyTask();
  approvalRequest: ApprovalRequest = this.emptyApprovalRequest();
  submitData: TaskApproveRequestSubmitData = {
    approvalDecision: ''
  };

  constructor(
    private client: ApproveRequestService,
    route: ActivatedRoute
  ) {
    const taskId: string = route.snapshot.paramMap.get('taskId');
    this.client.loadTaskApproveRequestFormData(taskId).subscribe(
      formData => {
        this.task = formData.task;
        this.approvalRequest = formData.approvalRequest;
      }, error => {
        console.log('Error loading approve request task with id', taskId);
      }
    );
  }

  complete() {
    console.log('Decision for', this.task.id, 'is', this.submitData.approvalDecision);
    this.client.submitTaskApproveRequestSubmitData(this.task.id, this.submitData).subscribe(
      result => {
        console.log('Sucessfully submitted');
      }, error => {
        console.log('Error submitting approve request task with id', this.task.id);
      }
    );
  }

  emptyTask(): Task {
    return {
      id: 'undefined',
      createTime: null,
      dueDate: null,
      name: 'undefined',
      description: 'undefined'
    };
  }

  emptyApprovalRequest(): ApprovalRequest {
    return {
      applicant: '',
      amount: '',
      id: 'undefined',
      subject: ''
    };
  }
}
