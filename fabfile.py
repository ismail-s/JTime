from fabric.api import run, cd
from fabric.contrib.project import rsync_project

def deploy():
    "Deploy the Rest api & website to production"
    with cd('JTime/JTime-rest'):
        run('git pull')
        run('npm install .')
        rsync_project(remote_dir = '~/JTime/JTime-rest/client/jtime-website',
                      local_dir = 'JTime-rest/client/jtime-website/build')
        run('pm2 reload .')
