export interface AppInterface {
  'key': string;
  'prozess': string;
  'taskName': string;
  'taskDescription'?: string;
  'creationDate': Date;
  'dueDate': Date;
  'modifiedAt': Date;
  'modifiedBy'?: Date;
  'priority': 'normal' | 'low' | 'high' | 'blocker';
  'indicator': 'claimed' | 'group' | 'claimed by other';
  'candidateGroups'?: string;
  'attributes'?: string;
  'targetUrl'?: string;
  'surce'?: string;
}


export interface TaskItem {
  task: Task,
  dataEntries: any[]
}

export interface Task {
  id: string,
  name: string,
  description: string,
  url: string,
  formKey: string,
  candidateGroups: string[],
  candidateUsers: string[],
  assignee: string,
  processName: string,
  createTime: Date,
  dueDate: Date,
  businessKey: string,
  priority: number,
  payload: Payload
}

export interface DataEntry {
  entryType: string,
  entryId: string,
  payload: ApprovalRequest
}

export interface Payload {
  request: string,
  originator: string,
  comment: any
}

export interface ApprovalRequest {
  amount: number,
  currency: string,
  id: string,
  subject: string,
  applicant: string

}
